package org.dc.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPForce;
import org.dc.jps.preset.JPReadOnly;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.exception.VerificationFailedException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.pattern.ObservableImpl;
import org.dc.jul.schedule.RecurrenceEventFilter;
import org.dc.jul.storage.registry.plugin.RegistryPlugin;
import org.dc.jul.storage.registry.plugin.RegistryPluginPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine Threepwood
 *
 * @param <KEY> EntryKey
 * @param <ENTRY> EntryType
 * @param <MAP> RegistryEntryMap
 * @param <R> Registry
 * @param <P> RegistryPluginType
 */
public class AbstractRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends Registry<KEY, ENTRY, R>, P extends RegistryPlugin<KEY, ENTRY>> extends ObservableImpl<Map<KEY, ENTRY>> implements Registry<KEY, ENTRY, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private String name;

    protected final MAP entryMap;

    protected final RegistryPluginPool<KEY, ENTRY, P> pluginPool;
    protected RegistrySandboxInterface<KEY, ENTRY, MAP, R> sandbox;

    protected boolean consistent;
    private final ReentrantReadWriteLock registryLock, consistencyCheckLock;

    private final List<ConsistencyHandler<KEY, ENTRY, MAP, R>> consistencyHandlerList;

    private RecurrenceEventFilter<String> consistencyFeedbackEventFilter;

    public AbstractRegistry(final MAP entryMap) throws InstantiationException {
        this(entryMap, new RegistryPluginPool<>());
    }

    public AbstractRegistry(final MAP entryMap, final RegistryPluginPool<KEY, ENTRY, P> pluginPool) throws InstantiationException {
        try {
            this.registryLock = new ReentrantReadWriteLock();
            this.consistencyCheckLock = new ReentrantReadWriteLock();
            this.consistent = true;
            this.entryMap = entryMap;
            this.pluginPool = pluginPool;
            this.pluginPool.init(this);
            this.sandbox = new MockRegistrySandbox<>();
            this.consistencyHandlerList = new ArrayList<>();
            this.consistencyFeedbackEventFilter = new RecurrenceEventFilter<String>(10000) {

                @Override
                public void relay() throws Exception {
                    logger.info(getLastValue());
                }
            };

            Runtime.getRuntime().addShutdownHook(new Thread(() -> {
                shutdown();
            }));
            finishTransaction();
            notifyObservers();
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public <S extends AbstractRegistry<KEY, ENTRY, MAP, R, P> & RegistrySandboxInterface<KEY, ENTRY, MAP, R>> void setupSandbox(final S sandbox) throws CouldNotPerformException {
        final RegistrySandboxInterface<KEY, ENTRY, MAP, R> oldSandbox = sandbox;
        try {
            this.sandbox = sandbox;
            this.sandbox.sync(entryMap);

            for (ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler : consistencyHandlerList) {
                this.sandbox.registerConsistencyHandler(consistencyHandler);
            }
        } catch (CouldNotPerformException ex) {
            this.sandbox = oldSandbox;
            throw new CouldNotPerformException("Could not setup sandbox!", ex);
        }
    }

    @Override
    public ENTRY register(final ENTRY entry) throws CouldNotPerformException {
        logger.info("Register " + entry + "...");
        pluginPool.beforeRegister(entry);
        try {
            checkWriteAccess();
            try {
                registryLock.writeLock().lock();
                if (entryMap.containsKey(entry.getId())) {
                    throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                }
                sandbox.register(entry);
                entryMap.put(entry.getId(), entry);
                finishTransaction();

            } finally {
                registryLock.writeLock().unlock();
            }
            notifyObservers();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + " in " + this + "!", ex);
        } finally {
            syncSandbox();
        }
        pluginPool.afterRegister(entry);
        return entry;
    }

    public ENTRY load(final ENTRY entry) throws CouldNotPerformException {
        logger.debug("Load " + entry + "...");
        pluginPool.beforeRegister(entry);
        try {
            try {
                registryLock.writeLock().lock();
                if (entryMap.containsKey(entry.getId())) {
                    throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                }
                sandbox.load(entry);
                entryMap.put(entry.getId(), entry);

            } finally {
                registryLock.writeLock().unlock();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + " in " + this + "!", ex);
        } finally {
            syncSandbox();
        }
        pluginPool.afterRegister(entry);
        return entry;
    }

    @Override
    public ENTRY update(final ENTRY entry) throws CouldNotPerformException {
        logger.info("Update " + entry + "...");
        for (int i = 0; i < Thread.currentThread().getStackTrace().length; ++i) {
            System.out.println(Thread.currentThread().getStackTrace()[i]);
        }
        pluginPool.beforeUpdate(entry);
        try {
            checkWriteAccess();
            try {
                // validate update
                registryLock.writeLock().lock();
                if (!entryMap.containsKey(entry.getId())) {
                    throw new InvalidStateException("Entry not registered!");
                }
                // perform update
                sandbox.update(entry);
                entryMap.put(entry.getId(), entry);
                finishTransaction();
            } finally {
                registryLock.writeLock().unlock();
            }
            notifyObservers();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update " + entry + " in " + this + "!", ex);
        } finally {
            syncSandbox();
        }
        pluginPool.afterUpdate(entry);
        return entry;
    }

    @Override
    public ENTRY remove(final KEY key) throws CouldNotPerformException {
        return remove(get(key));
    }

    @Override
    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException {
        return superRemove(entry);
    }

    public ENTRY superRemove(final ENTRY entry) throws CouldNotPerformException {
        logger.debug("Remove " + entry + "...");
        pluginPool.beforeRemove(entry);
        ENTRY oldEntry;
        try {
            checkWriteAccess();
            try {
                // validate removal
                registryLock.writeLock().lock();
                if (!entryMap.containsKey(entry.getId())) {
                    throw new InvalidStateException("Entry not registered!");
                }
                // perform removal
                sandbox.remove(entry);
                try {
                    oldEntry = entryMap.remove(entry.getId());
                } finally {
                    finishTransaction();
                }
            } finally {
                registryLock.writeLock().unlock();
            }
            notifyObservers();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove " + entry + " in " + this + "!", ex);
        } finally {
            syncSandbox();
        }
        pluginPool.afterRemove(entry);
        return oldEntry;
    }

    @Override
    public ENTRY get(final KEY key) throws CouldNotPerformException {
        verifyID(key);
        try {
            registryLock.readLock().lock();
            if (!entryMap.containsKey(key)) {
                TreeMap<KEY, ENTRY> sortedMap = new TreeMap<>((KEY o1, KEY o2) -> {
                    if (o1 instanceof String && o2 instanceof String) {
                        return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
                    } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        return ((Comparable) o1).compareTo(((Comparable) o2));
                    }
                    return (o1).toString().compareTo(o2.toString());
                });
                sortedMap.putAll(entryMap);

                if (sortedMap.floorKey(key) != null && sortedMap.ceilingKey(key) != null) {
                    throw new NotAvailableException("Entry", key.toString(), "Nearest neighbor is [" + sortedMap.floorKey(key) + "] or [" + sortedMap.ceilingKey(key) + "].");
                } else if (sortedMap.floorKey(key) != null) {
                    throw new NotAvailableException("Entry", key.toString(), "Nearest neighbor is Entry[" + sortedMap.floorKey(key) + "].");
                } else if (sortedMap.ceilingKey(key) != null) {
                    throw new NotAvailableException("Entry", key.toString(), "Nearest neighbor is Entry[" + sortedMap.ceilingKey(key) + "].");
                } else {
                    throw new NotAvailableException("Entry", key.toString(), new InvalidStateException("Registry is empty!"));
                }
            }
            pluginPool.beforeGet(key);
            return entryMap.get(key);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    @Override
    public List<ENTRY> getEntries() throws CouldNotPerformException {
        pluginPool.beforeGetEntries();
        try {
            registryLock.readLock().lock();
            return new ArrayList<>(entryMap.values());
        } finally {
            registryLock.readLock().unlock();
        }
    }

    @Override
    public int size() {
        try {
            registryLock.readLock().lock();
            return entryMap.size();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    public boolean isEmpty() {
        try {
            registryLock.readLock().lock();
            return entryMap.isEmpty();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    @Override
    public boolean contains(final ENTRY entry) throws CouldNotPerformException {
        return contains(entry.getId());
    }

    @Override
    public boolean contains(final KEY key) throws CouldNotPerformException {
        return entryMap.containsKey(verifyID(key));
    }

    @Override
    public void clear() throws CouldNotPerformException {
        pluginPool.beforeClear();
        try {
            registryLock.writeLock().lock();
            sandbox.clear();
            entryMap.clear();
            finishTransaction();
        } finally {
            registryLock.writeLock().unlock();
        }
        notifyObservers();
    }

    /**
     * Replaces the internal registry map by the given one. Use with care!
     *
     * @param map
     * @throws org.dc.jul.exception.CouldNotPerformException
     */
    public void replaceInternalMap(final Map<KEY, ENTRY> map) throws CouldNotPerformException {
        try {
            registryLock.writeLock().lock();
            try {
                sandbox.replaceInternalMap(map);
                entryMap.clear();
                entryMap.putAll(map);
                finishTransaction();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Internal map replaced by invalid data!", ex), logger, LogLevel.ERROR);
            } finally {
                syncSandbox();
            }
        } finally {
            registryLock.writeLock().unlock();
        }
        notifyObservers();
    }

    @Override
    public void checkWriteAccess() throws RejectedException {
        try {
            if (JPService.getProperty(JPForce.class).getValue()) {
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        try {
            if (JPService.getProperty(JPReadOnly.class).getValue()) {
                throw new RejectedException("ReadOnlyMode is detected!");
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        pluginPool.checkAccess();

        if (!consistent) {
            logger.warn("Registry is inconsistent! To fix registry manually start the registry in force mode.");
            throw new RejectedException("Registry is inconsistent!");
        }
    }

    @Override
    public boolean isReadOnly() {
        try {
            checkWriteAccess();
        } catch (RejectedException ex) {
            return true;
        }
        return false;
    }

    protected final void notifyObservers() {
        try {
            // It is not waited until the write actions are finished because the notification will be triggered after the lock release.
//            if (registryLock.isWriteLockedByCurrentThread()) {
//                logger.info("Notification of registry change skipped because of running write operations!");
//                return;
//            }
            super.notifyObservers(entryMap);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify all observer!", ex), logger, LogLevel.ERROR);
        }
    }

    protected KEY verifyID(final ENTRY entry) throws VerificationFailedException {
        try {
            return verifyID(entry.getId());
        } catch (CouldNotPerformException ex) {
            throw new VerificationFailedException("Could not verify message!", ex);
        }
    }

    protected KEY verifyID(final KEY id) throws VerificationFailedException {
        if (id == null) {
            throw new VerificationFailedException("Invalid id!", new NotAvailableException("id"));
        }

        if (id instanceof String && ((String) id).isEmpty()) {
            throw new VerificationFailedException("Invalid id!", new InvalidStateException("id is empty!"));
        }
        return id;
    }

    public void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException {
        consistencyHandlerList.add(consistencyHandler);
        sandbox.registerConsistencyHandler(consistencyHandler);
    }

    public void removeConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException {
        consistencyHandlerList.remove(consistencyHandler);
        sandbox.removeConsistencyHandler(consistencyHandler);
    }

    public final int checkConsistency() throws CouldNotPerformException {
        System.out.println("=========== start check " + getName() + " from thread " + Thread.currentThread().getId()
        );
        int modificationCounter = 0;

        if (consistencyHandlerList.isEmpty()) {
            logger.debug("Skip consistency check because no handler are registered.");
            return modificationCounter;
        }

        if (consistencyCheckLock.isWriteLockedByCurrentThread()) {
            // Avoid triggering recursive consistency checks.
            logger.info(getName() + " skipping consistency check check is already running by same thread. " + Thread.currentThread().getId());
            return modificationCounter;
        }

        try {
            registryLock.writeLock().lock();
            try {
                consistencyCheckLock.writeLock().lock();
                try {
                    int iterationCounter = 0;
                    MultiException.ExceptionStack exceptionStack = null;

                    final ArrayDeque<ConsistencyHandler> consistencyHandlerQueue = new ArrayDeque<>();
                    Object lastModifieredEntry = null;
                    int maxConsistencyChecks;
                    int errorCounter;
                    String note;

                    while (true) {

                        Thread.yield();

                        // handle handler interference
                        maxConsistencyChecks = consistencyHandlerList.size() * entryMap.size() * 2;
                        if (iterationCounter > maxConsistencyChecks) {
                            MultiException.checkAndThrow(MultiException.size(exceptionStack) + " errors occoured during processing!", exceptionStack);
                            throw new InvalidStateException("ConsistencyHandler" + Arrays.toString(consistencyHandlerQueue.toArray()) + " interference detected!");
                        }

                        // prepare for next iteraction
                        iterationCounter++;
                        if (exceptionStack != null) {
                            errorCounter = exceptionStack.size();
                            exceptionStack.clear();
                        } else {
                            errorCounter = 0;
                        }
                        if (!consistencyHandlerQueue.isEmpty() || errorCounter != 0) {

                            if (errorCounter > 0) {
                                note = " with " + errorCounter + " errors";
                            } else {
                                note = "";
                            }

                            if (!consistencyHandlerQueue.isEmpty()) {
                                note += " after " + consistencyHandlerQueue.size() + " applied modifications";
                            }
                            consistencyFeedbackEventFilter.trigger(((int) (((double) iterationCounter) / ((double) maxConsistencyChecks) * 100)) + "% of max consistency checks passed" + note + ".");
                        }
                        consistencyHandlerQueue.clear();

                        // consistency check
                        try {
                            for (ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler : consistencyHandlerList) {
                                consistencyHandler.reset();
                                for (ENTRY entry : entryMap.values()) {
                                    try {
                                        consistencyHandler.processData(entry.getId(), entry, entryMap, (R) this);
                                    } catch (CouldNotPerformException | NullPointerException ex) {
                                        logger.debug("Inconsisteny detected by ConsistencyHandler[" + consistencyHandler + "] in Entry[" + entry + "]!");
                                        exceptionStack = MultiException.push(consistencyHandler, new VerificationFailedException("Verification of Entry[" + entry.getId() + "] failed with " + consistencyHandler + "!", ex), exceptionStack);
                                    }
                                }
                            }
                        } catch (EntryModification ex) {

                            // check if consistency handler is looping
                            if (ex.getConsistencyHandler() == consistencyHandlerQueue.peekLast() && ex.getEntry().equals(lastModifieredEntry)) {
                                throw new InvalidStateException("ConsistencyHandler[" + consistencyHandlerQueue.peekLast() + "] is looping over same Entry[" + lastModifieredEntry + "] more than once!");
                            }

                            consistencyHandlerQueue.remove(ex.getConsistencyHandler());
                            consistencyHandlerQueue.offer(ex.getConsistencyHandler());
                            lastModifieredEntry = ex.getEntry();

                            // inform about modifications
                            logger.debug("Consistency modification applied: " + ex.getMessage());
                            modificationCounter++;
                            continue;
                        } catch (Throwable ex) {
                            throw new InvalidStateException("Fatal error occured during consistency check!", ex);
                        }

                        if (exceptionStack != null && !exceptionStack.isEmpty()) {
                            continue;
                        }

                        logger.debug("Registry consistend.");
                        break;
                    }
                    consistent = true;

                    if (modificationCounter > 0) {
                        consistencyFeedbackEventFilter.trigger("100% of consistency checks passed after " + modificationCounter + " applied modifications.");
                    }
                    System.out.println("=========== finish check " + getName());
                    return modificationCounter;

                } catch (CouldNotPerformException ex) {
                    consistent = false;
                    throw new CouldNotPerformException("Consistency process aborted!", ex);
                }
            } finally {
                consistencyCheckLock.writeLock().unlock();
            }
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    protected void finishTransaction() throws CouldNotPerformException {
        try {
            checkConsistency();
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("FATAL ERROR: Registry consistency check failed but sandbox check was successful!", ex), logger, LogLevel.ERROR);
        }
    }

    private void syncSandbox() throws CouldNotPerformException {
        try {
            registryLock.readLock().lock();
            sandbox.sync(entryMap);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    @Override
    public boolean isConsistent() {
        return consistent;
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            pluginPool.shutdown();

            consistencyHandlerList.stream().forEach((consistencyHandler) -> {
                consistencyHandler.shutdown();
            });

            clear();
            sandbox.clear();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
        }
    }

    /**
     * Method can be used to register any compatible registry plugin for this
     * registry.
     *
     * @param plugin the plugin to register.
     * @throws CouldNotPerformException is thrown in case the plugin could not
     * be registered.
     * @throws InterruptedException is thrown if the thread is externally
     * interrupted.
     */
    public void registerPlugin(P plugin) throws CouldNotPerformException, InterruptedException {
        pluginPool.addPlugin(plugin);
    }

    /**
     * Method defines the name of this registry.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Method returns the name of this registry. In case the name was never set
     * for this registry the simple class name of the registry class is returned
     * instead.
     *
     * @return the name of the registry.
     */
    @Override
    public String getName() {
        if (name == null) {
            return getClass().getSimpleName();
        }
        return name;

    }

    @Override
    public String toString() {
        return getName();
    }
}
