/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.storage.registry.plugin.RegistryPlugin;
import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPReadOnly;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.exception.VerificationFailedException;
import de.citec.jul.exception.printer.LogLevel;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.schedule.SyncObject;
import de.citec.jul.storage.registry.plugin.RegistryPluginPool;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine Threepwood
 *
 * @param <KEY> EntryKey
 * @param <ENTRY> EntryType
 * @param <MAP> RegistryEntryMap
 * @param <R> RegistryInterface
 * @param <P> RegistryPluginType
 */
public class AbstractRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>, P extends RegistryPlugin<KEY, ENTRY>> extends Observable<Map<KEY, ENTRY>> implements RegistryInterface<KEY, ENTRY, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final MAP entryMap;

    protected final RegistryPluginPool<KEY, ENTRY, P> pluginPool;
    protected RegistrySandboxInterface<KEY, ENTRY, MAP, R> sandbox;

    private final SyncObject consistencyCheckLock = new SyncObject("ConsistencyCheckLock");
    private boolean consistent;
    private final ReentrantReadWriteLock registryLock;

    private final List<ConsistencyHandler<KEY, ENTRY, MAP, R>> consistencyHandlerList;

    public AbstractRegistry(final MAP entryMap) throws InstantiationException {
        this(entryMap, new RegistryPluginPool<>());
    }

    public AbstractRegistry(final MAP entryMap, final RegistryPluginPool<KEY, ENTRY, P> pluginPool) throws InstantiationException {
        try {
            this.registryLock = new ReentrantReadWriteLock();
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
            throw new CouldNotPerformException("Could not register " + entry + "!", ex);
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
//                registryLock.readLock().lock();
                registryLock.writeLock().lock();
                if (entryMap.containsKey(entry.getId())) {
                    throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                }
//                registryLock.writeLock().lock();
                sandbox.load(entry);
                entryMap.put(entry.getId(), entry);

            } finally {
                registryLock.writeLock().unlock();
//                registryLock.readLock().unlock();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + "!", ex);
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
            throw new CouldNotPerformException("Could not update " + entry + "!", ex);
        } finally {
            syncSandbox();
        }
        pluginPool.afterUpdate(entry);
        return entry;
    }

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
            throw new CouldNotPerformException("Could not remove " + entry + "!", ex);
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
                TreeMap<KEY, ENTRY> sortedMap = new TreeMap<>(new Comparator<KEY>() {
                    @Override
                    public int compare(KEY o1, KEY o2) {
                        if (o1 instanceof String && o2 instanceof String) {
                            return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
                        }
                        return ((Comparable<KEY>) o1).compareTo(o2);
                    }
                });
                sortedMap.putAll(entryMap);

                if (sortedMap.floorKey(key) != null && sortedMap.ceilingKey(key) != null) {
                    throw new NotAvailableException("Entry[" + key + "]", "Nearest neighbor is [" + sortedMap.floorKey(key) + "] or [" + sortedMap.ceilingKey(key) + "].");
                } else if (sortedMap.floorKey(key) != null) {
                    throw new NotAvailableException("Entry[" + key + "]", "Nearest neighbor is Empty[" + sortedMap.floorKey(key) + "].");
                } else if (sortedMap.ceilingKey(key) != null) {
                    throw new NotAvailableException("Entry[" + key + "]", "Nearest neighbor is Empty[" + sortedMap.ceilingKey(key) + "].");
                } else {
                    throw new NotAvailableException("Entry[" + key + "]", "Registry is empty!");
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
        if (JPService.getProperty(JPReadOnly.class).getValue()) {
            throw new RejectedException("ReadOnlyMode is detected!");
        }

        if (!consistent) {
            logger.warn("Registry is inconsistent! To fix registry manually start the registry in force mode.");
            throw new RejectedException("Registry is inconsistent!");
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

    private boolean consistencyCheckRunning = false;

    public final boolean checkConsistency() throws CouldNotPerformException {

        if (consistencyHandlerList.isEmpty()) {
            logger.debug("Skip consistency check because no handler are registered.");
            return true;
        }

        synchronized (consistencyCheckLock) {
            // avoid dublicated consistency checks
            if (consistencyCheckRunning) {
                return false;
            }
            consistencyCheckRunning = true;
        }

        try {
            registryLock.writeLock().lock();
            try {

                boolean modification = false;

                int iterationCounter = 0;
                MultiException.ExceptionStack exceptionStack = null;

                ConsistencyHandler lastActiveConsistencyHandler = null;
                Object lastModifieredEntry = null;

                while (true) {

                    Thread.yield();

                    // handle handler interference
                    if (iterationCounter > consistencyHandlerList.size() * entryMap.size() * 2) {
                        MultiException.checkAndThrow("To many errors occoured during processing!", exceptionStack);
                        throw new InvalidStateException("ConsistencyHandler interference detected!");
                    }

                    if (exceptionStack != null) {
                        exceptionStack.clear();
                    }

                    iterationCounter++;
                    try {
                        for (ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler : consistencyHandlerList) {
                            consistencyHandler.reset();
                            for (ENTRY entry : entryMap.values()) {
                                try {
                                    consistencyHandler.processData(entry.getId(), entry, entryMap, (R) this);
                                } catch (CouldNotPerformException | NullPointerException ex) {
                                    exceptionStack = MultiException.push(consistencyHandler, new VerificationFailedException("Could not verify registry data consistency!", ex), exceptionStack);
                                }
                            }
                        }
                    } catch (EntryModification ex) {

                        // check if consistency handler is looping
                        if (ex.getConsistencyHandler() == lastActiveConsistencyHandler && ex.getEntry().equals(lastModifieredEntry)) {
                            throw new InvalidStateException("ConsistencyHandler[" + lastActiveConsistencyHandler + "] is looping over same Entry[" + lastModifieredEntry + "] more than once!");
                        }
                        lastActiveConsistencyHandler = ex.getConsistencyHandler();
                        lastModifieredEntry = ex.getEntry();

                        // inform about modifications
                        logger.info("Consistency modification applied: " + ex.getMessage());
                        modification = true;
                        continue;
                    } catch (Throwable ex) {
                        exceptionStack = MultiException.push(this, new InvalidStateException("Fatal error occured during consistency check!", ex), exceptionStack);
                    }

                    if (exceptionStack != null && !exceptionStack.isEmpty()) {
                        continue;
                    }

                    logger.debug("Registry consistend.");
                    break;
                }
                synchronized (consistencyCheckLock) {
                    consistencyCheckRunning = false;
                }
                consistent = true;
                return modification;

            } catch (CouldNotPerformException ex) {
                consistent = false;
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Consistency process aborted!", ex), logger, LogLevel.ERROR);
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
    public void shutdown() {
        super.shutdown();
        try {
            pluginPool.shutdown();
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
