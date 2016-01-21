/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

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
import org.dc.jul.pattern.Observable;
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
public class AbstractRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends Registry<KEY, ENTRY, R>, P extends RegistryPlugin<KEY, ENTRY>> extends Observable<Map<KEY, ENTRY>> implements Registry<KEY, ENTRY, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final MAP entryMap;

    protected final RegistryPluginPool<KEY, ENTRY, P> pluginPool;
    protected RegistrySandboxInterface<KEY, ENTRY, MAP, R> sandbox;

    protected boolean consistent;
    private final ReentrantReadWriteLock registryLock, consistencyCheckLock;

    private final List<ConsistencyHandler<KEY, ENTRY, MAP, R>> consistencyHandlerList;

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
        logger.debug("Register " + entry + "...");
        pluginPool.beforeRegister(entry);
        try {
            checkAccess();
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
        logger.debug("Update " + entry + "...");
        pluginPool.beforeUpdate(entry);
        try {
            checkAccess();
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
            checkAccess();
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
    public void checkAccess() throws RejectedException {
        try {
            if (JPService.getProperty(JPReadOnly.class).getValue()) {
                throw new RejectedException("ReadOnlyMode is detected!");
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        pluginPool.checkAccess();

        try {
            if (!consistent && !JPService.getProperty(JPForce.class).getValue()) {
                logger.warn("Registry is inconsistent! To fix registry manually start the registry in force mode.");
                throw new RejectedException("Registry is inconsistent!");
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }
    }

    @Override
    public boolean isReadOnly() {
        try {
            checkAccess();
        } catch (RejectedException ex) {
            return true;
        }
        return false;
    }

    protected void notifyObservers() {
        try {
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

        int modificationCounter = 0;

        if (consistencyHandlerList.isEmpty()) {
            logger.debug("Skip consistency check because no handler are registered.");
            return modificationCounter;
        }

        if (consistencyCheckLock.isWriteLockedByCurrentThread()) {
            // Avoid triggering recursive consistency checks.
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
//                    ConsistencyHandler lastActiveConsistencyHandler = null;
                    Object lastModifieredEntry = null;

                    while (true) {

                        Thread.yield();

                        // handle handler interference
                        if (iterationCounter > consistencyHandlerList.size() * entryMap.size() * 2) {
                            MultiException.checkAndThrow("To many errors occoured during processing!", exceptionStack);
                            throw new InvalidStateException("ConsistencyHandler" + Arrays.toString(consistencyHandlerQueue.toArray()) + " interference detected!");
                        }

                        if (exceptionStack != null) {
                            exceptionStack.clear();
                        }

                        consistencyHandlerQueue.clear();

                        iterationCounter++;
                        try {
                            for (ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler : consistencyHandlerList) {
                                consistencyHandler.reset();
                                for (ENTRY entry : entryMap.values()) {
                                    try {
                                        consistencyHandler.processData(entry.getId(), entry, entryMap, (R) this);
                                    } catch (CouldNotPerformException | NullPointerException ex) {
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
                            logger.info("Consistency modification applied: " + ex.getMessage());
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

    public void registerPlugin(P plugin) throws CouldNotPerformException {
        pluginPool.addPlugin(plugin);
    }

    @Override
    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
