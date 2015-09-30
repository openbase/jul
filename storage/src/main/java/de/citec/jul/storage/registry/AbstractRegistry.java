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
import de.citec.jul.exception.VerificationFailedException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.schedule.SyncObject;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
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
public class AbstractRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>, P extends RegistryPlugin> extends Observable<Map<KEY, ENTRY>> implements RegistryInterface<KEY, ENTRY, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final MAP entryMap;
    
    // TODO mpohling: Implement as plugin pool.
    protected final List<P> pluginList;
    protected RegistrySandboxInterface<KEY, ENTRY, MAP, R> sandbox;

    private final SyncObject SYNC = new SyncObject(AbstractRegistry.class);
    private final List<ConsistencyHandler<KEY, ENTRY, MAP, R>> consistencyHandlerList;

    public AbstractRegistry(final MAP entryMap) throws InstantiationException {
        try {
            this.entryMap = entryMap;
            this.pluginList = new ArrayList<>();
            this.sandbox = new MockRegistrySandbox();
            this.consistencyHandlerList = new ArrayList<>();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    shutdown();
                }
            }));
            finishTransaction();
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
        pluginList.stream().forEach((plugin) -> {
            plugin.beforeRegister(entry);
        });
        try {
            checkAccess();
            synchronized (SYNC) {
                if (entryMap.containsKey(entry.getId())) {
                    throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                }
                sandbox.register(entry);
                entryMap.put(entry.getId(), entry);
                finishTransaction();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + "!", ex);
        } finally {
            syncSandbox();
        }
        pluginList.stream().forEach((plugin) -> {
            plugin.afterRegister(entry);
        });
        return entry;
    }

    @Override
    public ENTRY update(final ENTRY entry) throws CouldNotPerformException {
        logger.info("Update " + entry + "...");
        try {
            checkAccess();
            synchronized (SYNC) {
                if (!entryMap.containsKey(entry.getId())) {
                    throw new InvalidStateException("Entry not registered!");
                }
                // replace
                sandbox.update(entry);
                entryMap.put(entry.getId(), entry);
                finishTransaction();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update " + entry + "!", ex);
        } finally {
            syncSandbox();
        }
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
        logger.info("Remove " + entry + "...");
        try {
            checkAccess();
            synchronized (SYNC) {
                if (!entryMap.containsKey(entry.getId())) {
                    throw new InvalidStateException("Entry not registered!");
                }
                sandbox.remove(entry);
                try {
                    return entryMap.remove(entry.getId());
                } finally {
                    finishTransaction();
                }
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove " + entry + "!", ex);
        } finally {
            syncSandbox();
        }
    }

    @Override
    public ENTRY get(final KEY key) throws CouldNotPerformException {
        verifyID(key);
        synchronized (SYNC) {
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
            return entryMap.get(key);
        }
    }

    @Override
    public List<ENTRY> getEntries() {
        synchronized (SYNC) {
            return new ArrayList<>(entryMap.values());
        }
    }

    public int size() {
        synchronized (SYNC) {
            return entryMap.size();
        }
    }

    public boolean isEmpty() {
        synchronized (SYNC) {
            return entryMap.isEmpty();
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
        synchronized (SYNC) {
            sandbox.clear();
            entryMap.clear();
        }
        finishTransaction();
    }

    public void replaceInternalMap(final Map<KEY, ENTRY> map) {
        synchronized (SYNC) {
            try {
                sandbox.replaceInternalMap(map);
                entryMap.clear();
                entryMap.putAll(map);
                finishTransaction();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Internal map replaced by invalid data!", ex));
            } finally {
                syncSandbox();
            }
        }
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        if (JPService.getProperty(JPReadOnly.class).getValue()) {
            throw new InvalidStateException("ReadOnlyMode is detected!");
        }
    }

    @Override
    public boolean isReadOnly() {
        try {
            checkAccess();
        } catch (InvalidStateException ex) {
            return true;
        }
        return false;
    }

    private void notifyObservers() {
        try {
            super.notifyObservers(entryMap);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not notify all observer!", ex));
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

        synchronized (SYNC) {

            try {

                boolean modification = false;

                // avoid dublicated consistency check
                if (consistencyCheckRunning) {
                    return modification;
                }
                consistencyCheckRunning = true;

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
//                        System.out.println("#### ch:" + lastActiveConsistencyHandler + " = " + lastModifieredEntry);
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

                    logger.info("Registry consistend.");
                    break;
                }

                consistencyCheckRunning = false;
                return modification;

            } catch (CouldNotPerformException ex) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Consistency process aborted!", ex));
            }
        }
    }

    protected void finishTransaction() throws CouldNotPerformException {
        try {
            checkConsistency();
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("FATAL ERROR: Registry consistency check failed but sandbox check was successful!", ex));
        }
        notifyObservers();
    }

    private void syncSandbox() {
        synchronized (SYNC) {
            sandbox.sync(entryMap);
        }
    }

    @Override
    public void shutdown() {
        super.shutdown();
        try {
            clear();
            sandbox.clear();
            pluginList.stream().forEach((plugin) -> {
                plugin.shutdown();
            });
            pluginList.clear();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(logger, ex);
        }
    }

    public void addPlugin(P plugin) throws CouldNotPerformException {
        pluginList.add(plugin);
        try {
            plugin.init();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not add Plugin[" + plugin.getClass().getName() + "] to Registry[" + getClass().getSimpleName() + "]", ex);
        }
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
