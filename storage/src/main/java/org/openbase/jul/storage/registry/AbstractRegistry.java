package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPForce;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jps.preset.JPVerbose;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.exception.printer.LogLevelFilter;
import org.openbase.jul.exception.printer.Printer;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.pattern.ChangeHandler;
import org.openbase.jul.pattern.ChangeListener;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.RecurrenceEventFilter;
import org.openbase.jul.storage.registry.plugin.RegistryPlugin;
import org.openbase.jul.storage.registry.plugin.RegistryPluginPool;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * @param <KEY>      EntryKey
 * @param <ENTRY>    EntryType
 * @param <MAP>      RegistryEntryMap
 * @param <REGISTRY> Registry
 * @param <PLUGIN>   RegistryPluginType
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractRegistry<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, REGISTRY extends Registry<KEY, ENTRY>, PLUGIN extends RegistryPlugin<KEY, ENTRY, REGISTRY>> extends ObservableImpl<DataProvider<Map<KEY, ENTRY>>, Map<KEY, ENTRY>> implements Registry<KEY, ENTRY> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    protected final RegistryPluginPool<KEY, ENTRY, PLUGIN, REGISTRY> pluginPool;
    private final MAP entryMap;

    private final Random randomJitter;
    private final ReentrantReadWriteLock registryLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock dependingRegistryMapLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock consistencyCheckLock = new ReentrantReadWriteLock();
    private final Set<Registry> lockedRegistries = new HashSet<>();
    private final List<ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY>> consistencyHandlerList;
    /**
     * Map of registries this one depends on.
     */
    private final Map<Registry, DependencyConsistencyCheckTrigger> dependingRegistryMap;
    private final ObservableImpl<Registry<KEY, ENTRY>, Map<KEY, ENTRY>> dependingRegistryObservable;
    private final ChangeHandler transactionPerformedNotifier;
    protected RegistrySandbox<KEY, ENTRY, MAP, REGISTRY> sandbox;
    protected boolean consistent;
    private String name;
    private int lockCounter = 0;
    private RecurrenceEventFilter<String> consistencyFeedbackEventFilter;

    private boolean notificationSkipped;

    private boolean shutdownInitiated = false;

    public AbstractRegistry(final MAP entryMap) throws InstantiationException {
        this(entryMap, new RegistryPluginPool<>());
    }

    public AbstractRegistry(final MAP entryMap, final RegistryPluginPool<KEY, ENTRY, PLUGIN, REGISTRY> pluginPool) throws InstantiationException {
        super();
        try {

            // validate arguments
            if (entryMap == null) {
                throw new NotAvailableException("entryMap");
            }

            if (pluginPool == null) {
                throw new NotAvailableException("pluginPool");
            }

            this.randomJitter = new Random(System.currentTimeMillis());
            this.consistent = true;
            this.notificationSkipped = false;
            this.entryMap = entryMap;
            this.pluginPool = pluginPool;
            try {
                this.pluginPool.init((REGISTRY) this);
            } catch (ClassCastException ex) {
                throw new InstantiationException(this, new InvalidStateException("Registry not compatible with registered plugin pool!"));
            }
            this.consistencyHandlerList = new ArrayList<>();
            this.dependingRegistryMap = new HashMap<>();
            this.sandbox = new MockRegistrySandbox<>(this);
            this.dependingRegistryObservable = new ObservableImpl<>(this);
            this.transactionPerformedNotifier = new ChangeHandler();

            this.consistencyFeedbackEventFilter = new RecurrenceEventFilter<String>(10000) {
                @Override
                public void relay() throws Exception {
                    log(getLatestValue());
                }
            };
            setHashGenerator(value -> {
                registryLock.readLock().lock();
                try {
                    return value.hashCode();
                } finally {
                    registryLock.readLock().unlock();
                }
            });

            finishTransaction();
            notifyObservers();
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    protected <S extends AbstractRegistry<KEY, ENTRY, MAP, REGISTRY, PLUGIN> & RegistrySandbox<KEY, ENTRY, MAP, REGISTRY>> void setupSandbox(final S sandbox) throws CouldNotPerformException {
        final RegistrySandbox<KEY, ENTRY, MAP, REGISTRY> oldSandbox = this.sandbox;
        try {
            if (sandbox == null) {
                throw new NotAvailableException("sandbox");
            }

            this.sandbox = sandbox;
            this.sandbox.sync(entryMap);

            for (ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler : consistencyHandlerList) {
                this.sandbox.registerConsistencyHandler(consistencyHandler);
            }
        } catch (CouldNotPerformException ex) {
            this.sandbox = oldSandbox;
            throw new CouldNotPerformException("Could not setup sandbox!", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param entry {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public ENTRY register(final ENTRY entry) throws CouldNotPerformException {
        if (entry == null) {
            throw new NotAvailableException("entry");
        }
        log("Register " + entry + "...");
        try {
            checkWriteAccess();
            lock();
            try {
                try {
                    if (entryMap.containsKey(entry.getId())) {
                        throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                    }
                    sandbox.register(entry);
                    pluginPool.beforeRegister(entry);
                    entryMap.put(entry.getId(), entry);
                    finishTransaction();
                    pluginPool.afterRegister(entry);
                } finally {
                    syncSandbox();
                }
            } finally {
                unlock();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + " in " + this + "!", ex);
        }
        notifySuccessfulTransaction();
        notifyObservers();
        return get(entry);
    }

    public ENTRY load(final ENTRY entry) throws CouldNotPerformException {
        if (entry == null) {
            throw new NotAvailableException("entry");
        }
        logger.debug("Load " + entry + "...");
        try {
            lock();
            try {
                try {
                    if (entryMap.containsKey(entry.getId())) {
                        throw new CouldNotPerformException("Could not register " + entry + "! Entry with same Id[" + entry.getId() + "] already registered!");
                    }
                    sandbox.load(entry);
                    pluginPool.beforeRegister(entry);
                    entryMap.put(entry.getId(), entry);
                    pluginPool.afterRegister(entry);
                } finally {
                    syncSandbox();
                }
            } finally {
                unlock();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register " + entry + " in " + this + "!", ex);
        }
        return entry;
    }

    /**
     * {@inheritDoc}
     *
     * @param entry {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public ENTRY update(final ENTRY entry) throws CouldNotPerformException {
        if (entry == null) {
            throw new NotAvailableException("entry");
        }
        log("Update " + entry + "...");
        try {
            checkWriteAccess();
            lock();
            try {
                try {
                    // validate update
                    if (!entryMap.containsKey(entry.getId())) {
                        throw new InvalidStateException("Entry not registered!");
                    }
                    // perform update
                    sandbox.update(entry);
                    // check if the new message results in an update after consistency checks
                    final boolean changed = !isSandbox() && ((sandbox instanceof MockRegistrySandbox) || !get(entry).equals(sandbox.get(entry)));
                    pluginPool.beforeUpdate(entry);
                    entryMap.put(entry.getId(), entry);
                    finishTransaction();
                    pluginPool.afterUpdate(entry);
                    // test if the entry has changed at all by this update method
                    if (changed) {
                        notifySuccessfulTransaction();
                    }
                } finally {
                    syncSandbox();
                }
            } finally {
                unlock();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update " + entry + " in " + this + "!", ex);
        }
        notifyObservers();
        return get(entry);
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public ENTRY remove(final KEY key) throws CouldNotPerformException {
        return remove(get(key));
    }

    /**
     * {@inheritDoc}
     *
     * @param entry {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException {
        return superRemove(entry);
    }

    public ENTRY superRemove(final ENTRY entry) throws CouldNotPerformException {
        if (entry == null) {
            throw new NotAvailableException("entry");
        }
        log("Remove " + entry + "...");
        ENTRY oldEntry;
        try {
            checkWriteAccess();
            lock();
            try {
                try {
                    // validate removal
                    if (!entryMap.containsKey(entry.getId())) {
                        throw new InvalidStateException("Entry not registered!");
                    }
                    // perform removal
                    pluginPool.beforeRemove(entry);
                    sandbox.remove(entry);
                    try {
                        oldEntry = entryMap.remove(entry.getId());
                    } finally {
                        finishTransaction();
                    }
                    pluginPool.afterRemove(entry);
                } finally {
                    syncSandbox();
                }
            } finally {
                unlock();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove " + entry + " in " + this + "!", ex);
        }
        notifySuccessfulTransaction();
        notifyObservers();
        return oldEntry;
    }

    /**
     * {@inheritDoc}
     *
     * @param entries {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws MultiException        {@inheritDoc}
     * @throws InvalidStateException {@inheritDoc}
     */
    @Override
    public List<ENTRY> removeAll(Collection<ENTRY> entries) throws MultiException, InvalidStateException {
        final List<ENTRY> removedEntries = new ArrayList<>();
        ExceptionStack exceptionStack = null;
        registryLock.writeLock().lock();
        try {
            for (ENTRY entry : entries) {

                // shutdown check is needed because this is not a atomic transaction.
                if (shutdownInitiated) {
                    throw new InvalidStateException("Entry removal canceled because registry is shutting down!");
                }

                try {
                    if (contains(entry)) {
                        removedEntries.add(remove(entry));
                    }
                } catch (CouldNotPerformException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
            }
            MultiException.checkAndThrow(() -> "Could not remove all entries!", exceptionStack);
        } finally {
            registryLock.writeLock().unlock();
        }
        return removedEntries;
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public ENTRY get(final KEY key) throws CouldNotPerformException {
        if (key == null) {
            throw new NotAvailableException("key");
        }
        verifyID(key);
        registryLock.readLock().lock();
        try {
            if (!entryMap.containsKey(key)) {

                if (entryMap.isEmpty()) {
                    throw new NotAvailableException("Entry", key.toString(), new InvalidStateException(this + " is empty!"));
                }

                TreeMap<KEY, ENTRY> sortedMap = new TreeMap<>((KEY o1, KEY o2) -> {
                    if (o1 instanceof String && o2 instanceof String) {
                        return ((String) o1).toLowerCase().compareTo(((String) o2).toLowerCase());
                    } else if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        return ((Comparable) o1).compareTo((o2));
                    }
                    return (o1).toString().compareTo(o2.toString());
                });
                sortedMap.putAll(entryMap);

                if (sortedMap.floorKey(key) != null && sortedMap.ceilingKey(key) != null) {
                    throw new NotAvailableException("Entry", key.toString(), "Nearest neighbor is " + get(sortedMap.floorKey(key)) + " or " + get(sortedMap.ceilingKey(key)) + ".");
                } else if (sortedMap.floorKey(key) != null) {
                    throw new NotAvailableException("Entry", key.toString(), "Nearest neighbor is " + get(sortedMap.floorKey(key)) + ".");
                } else if (sortedMap.ceilingKey(key) != null) {
                    throw new NotAvailableException("Entry", key.toString(), "Nearest neighbor is " + get(sortedMap.ceilingKey(key)) + ".");
                } else {
                    throw new InvalidStateException("Implementation error, case not handled.");
                }
            }
            pluginPool.beforeGet(key);
            return entryMap.get(key);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public List<ENTRY> getEntries() {
        registryLock.readLock().lock();
        try {
            return new ArrayList<>(entryMap.values());
        } finally {
            registryLock.readLock().unlock();
        }
    }

    @Override
    public Map<KEY, ENTRY> getEntryMap() {
        registryLock.readLock().lock();
        try {
            return Collections.unmodifiableMap(entryMap);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public int size() {
        registryLock.readLock().lock();
        try {
            return entryMap.size();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isEmpty() {
        registryLock.readLock().lock();
        try {
            return entryMap.isEmpty();
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param entry {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public boolean contains(final ENTRY entry) throws CouldNotPerformException {
        if (entry == null) {
            throw new NotAvailableException("entry");
        }
        return contains(entry.getId());
    }

    /**
     * {@inheritDoc}
     *
     * @param key {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean contains(final KEY key) {
        try {
            verifyID(key);
        } catch (VerificationFailedException ex) {
            return false;
        }
        return entryMap.containsKey(key);
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void clear() throws CouldNotPerformException {
        lock();
        try {
            pluginPool.beforeClear();
            sandbox.clear();
            entryMap.clear();
            consistent = true;
        } finally {
            unlock();
        }
        notifyObservers();
    }

    /**
     * Replaces the internal registry map by the given one.
     * <p>
     * Use with care!
     *
     * @param map
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public void replaceInternalMap(final Map<KEY, ENTRY> map) throws CouldNotPerformException {
        replaceInternalMap(map, true);
    }

    public Class getEntryMapClass() {
        return entryMap.getClass();
    }

    /**
     * Replaces the internal registry map by the given one.
     * <p>
     * Use with care!
     *
     * @param map
     * @param finishTransaction is true the registry transaction will be verified.
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public void replaceInternalMap(final Map<KEY, ENTRY> map, boolean finishTransaction) throws CouldNotPerformException {
        if (map == null) {
            throw new NotAvailableException("map");
        }
        lock();
        try {
            try {
                sandbox.replaceInternalMap(map);
                entryMap.clear();
                entryMap.putAll(map);
                if (finishTransaction && !(this instanceof RemoteRegistry)) {
                    logger.warn("Replace internal map of [" + this + "]");
                    finishTransaction();
                }
            } finally {
                syncSandbox();
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Internal map replaced by invalid data!", ex), logger, LogLevel.ERROR);
        } finally {
            unlock();
        }
        if (this instanceof RemoteRegistry) {
            dependingRegistryObservable.notifyObservers(entryMap);
        }
        notifyObservers();
    }

    /**
     * {@inheritDoc}
     *
     * @throws RejectedException {@inheritDoc}
     */
    @Override
    public void checkWriteAccess() throws RejectedException {
        logger.debug("checkWriteAccess of " + this);

        if (!isDependingOnConsistentRegistries()) {
            throw new RejectedException("At least one depending registry is inconsistent!");
        }

        pluginPool.checkAccess();

        if (!consistent) {
            log(getName() + " is inconsistent! To fix registry manually start the registry in force mode.", LogLevel.WARN);
            throw new RejectedException("Registry is inconsistent!");
        }
    }

    /**
     * If this registry depends on other registries, this method can be used to tell this registry if all depending registries are consistent.
     *
     * @return The method returns should return false if at least one depending registry is not consistent!
     */
    protected boolean isDependingOnConsistentRegistries() {
        dependingRegistryMapLock.readLock().lock();
        try {
            for (Registry registry : dependingRegistryMap.keySet()) {
                if (!registry.isConsistent()) {
                    return false;
                }
            }
            return true;
        } finally {
            dependingRegistryMapLock.readLock().unlock();
        }
    }

    /**
     * If this registry depends on other registries, this method can be used to tell this registry all depending registries.
     * For instance this is used to switch to read only mode in case one depending registry is not consistent.
     * Consistency checks are skipped as well if at least one depending registry is not consistent.
     * <p>
     * NOTE: the registered dependencies have to be activated later by calling {@link #activateDependencies()}.
     *
     * @param registry the dependency of these registry.
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public void registerDependency(final Registry registry) throws CouldNotPerformException {
        if (registry == null) {
            throw new NotAvailableException("registry");
        }
        registryLock.writeLock().lock();
        try {
            dependingRegistryMapLock.writeLock().lock();
            try {
                // check if already registered
                if (dependingRegistryMap.containsKey(registry)) {
                    return;
                }
                dependingRegistryMap.put(registry, new DependencyConsistencyCheckTrigger(registry));
            } finally {
                dependingRegistryMapLock.writeLock().unlock();
            }
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Activate all {@code DependencyConsistencyCheckTrigger} added by calling {@link #registerDependency(Registry)}.
     *
     * @throws CouldNotPerformException if the activation fails
     * @throws InterruptedException     if the activation is interrupted
     */
    public void activateDependencies() throws CouldNotPerformException, InterruptedException {
        dependingRegistryMapLock.readLock().lock();
        try {
            for (final DependencyConsistencyCheckTrigger dependencyConsistencyCheckTrigger : dependingRegistryMap.values()) {
                dependencyConsistencyCheckTrigger.activate();
            }
        } finally {
            dependingRegistryMapLock.readLock().unlock();
        }
    }

    /**
     * This method allows the removal of a registered registry dependency.
     *
     * @param registry the dependency to remove.
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException
     */
    public void removeDependency(final Registry registry) throws CouldNotPerformException {
        if (registry == null) {
            throw new NotAvailableException("registry");
        }
        registryLock.writeLock().lock();
        try {
            dependingRegistryMapLock.writeLock().lock();
            try {
                if (!dependingRegistryMap.containsKey(registry)) {
                    logger.warn("Could not remove a dependency which was never registered!");
                    return;
                }
                dependingRegistryMap.remove(registry).shutdown();
            } finally {
                dependingRegistryMapLock.writeLock().unlock();
            }
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * Removal of all registered registry dependencies in the reversed order in which they where added.
     */
    public void removeAllDependencies() {
        registryLock.writeLock().lock();
        try {
            dependingRegistryMapLock.writeLock().lock();
            try {
                List<Registry> dependingRegistryList = new ArrayList<>(dependingRegistryMap.keySet());
                Collections.reverse(dependingRegistryList);
                for (Registry registry : dependingRegistryList) {
                    dependingRegistryMap.remove(registry).shutdown();
                }
            } finally {
                dependingRegistryMapLock.writeLock().unlock();
            }
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isReadOnly() {
        try {
            checkWriteAccess();
        } catch (RejectedException ex) {
            return true;
        }
        return false;
    }

    /**
     * Method triggers a new registry changed notification on all observers.
     * The notification will be skipped in case the registry is busy. But a final notification is always guaranteed.
     *
     * @return true if the notification was performed or false if the notification was skipped.
     */
    protected final boolean notifyObservers() {
        try {
            // It is not waited until the write actions are finished because the notification will be triggered after the lock release.
            if (registryLock.isWriteLockedByCurrentThread()) {
                logger.debug("Notification of registry[" + this + "] change skipped because of running write operations!");
                notificationSkipped = true;
                return false;
            }

            if (super.notifyObservers(entryMap)) {
                try {
                    pluginPool.afterRegistryChange();
                } catch (CouldNotPerformException ex) {
                    MultiException.ExceptionStack exceptionStack = new MultiException.ExceptionStack();
                    exceptionStack.push(pluginPool, ex);
                    throw new MultiException("PluginPool could not execute afterRegistryChange", exceptionStack);
                }
            }
            notificationSkipped = false;
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify all registry observer!", ex), logger, LogLevel.ERROR);
            return false;
        }
        return true;
    }

    protected KEY verifyID(final ENTRY entry) throws VerificationFailedException {
        try {
            if (entry == null) {
                throw new NotAvailableException("entry");
            }
            return verifyID(entry.getId());
        } catch (CouldNotPerformException ex) {
            throw new VerificationFailedException("Could not verify message!", ex);
        }
    }

    protected KEY verifyID(final KEY key) throws VerificationFailedException {
        if (key == null) {
            throw new VerificationFailedException("Invalid id!", new NotAvailableException("id"));
        }

        if (key instanceof String && ((String) key).isEmpty()) {
            throw new VerificationFailedException("Invalid id!", new InvalidStateException("id is empty!"));
        }
        return key;
    }

    public void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler) throws CouldNotPerformException {
        try {
            if (consistencyHandler == null) {
                throw new NotAvailableException("consistencyHandler");
            }
            consistencyHandlerList.add(consistencyHandler);
            sandbox.registerConsistencyHandler(consistencyHandler);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register ConsistencyHandler[" + consistencyHandler + "]", ex);
        }
    }

    public void removeConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler) throws CouldNotPerformException {
        try {
            if (consistencyHandler == null) {
                throw new NotAvailableException("consistencyHandler");
            }
            consistencyHandlerList.remove(consistencyHandler);
            sandbox.removeConsistencyHandler(consistencyHandler);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not remove ConsistencyHandler[" + consistencyHandler + "]", ex);
        }
    }

    public void waitForRemoteDependencies() throws CouldNotPerformException, InterruptedException {
        final ArrayList<Registry> dependingRegistries;

        // copy dep list to avoid locking while wait.
        dependingRegistryMapLock.readLock().lock();
        try {
            dependingRegistries = new ArrayList<>(dependingRegistryMap.keySet());
        } finally {
            dependingRegistryMapLock.readLock().unlock();
        }

        // perform wait
        for (final Registry dependency : dependingRegistries) {
            if (dependency instanceof RemoteRegistry) {
                ((RemoteRegistry) dependency).waitUntilReady();
            }
        }
    }

    @SuppressWarnings("UseSpecificCatch")
    public final int checkConsistency() throws CouldNotPerformException {
        int modificationCounter = 0;

        if (consistencyHandlerList.isEmpty()) {
            logger.debug("Skip consistency check because no handler are registered.");
            return modificationCounter;
        }

        if (isEmpty()) {
            logger.debug("Skip consistency check because " + getName() + " is empty.");
            return modificationCounter;
        }

        if (!isDependingOnConsistentRegistries()) {
            logger.warn("Skip consistency check because " + getName() + " is depending on at least one inconsistent registry!");
            return modificationCounter;
        }

        if (consistencyCheckLock.isWriteLockedByCurrentThread()) {
            // Avoid triggering recursive consistency checks.
            logger.debug(this + " skipping consistency check because check is already running by same thread: " + Thread.currentThread().getId());
            return modificationCounter;
        }

        lock();
        try {
            pluginPool.beforeConsistencyCheck();
            consistencyCheckLock.writeLock().lock();
            try {
                try {
                    try {
                        int iterationCounter = 0;
                        MultiException.ExceptionStack exceptionStack = null, previousExceptionStack = null;

                        final ArrayDeque<ConsistencyHandler> consistencyHandlerQueue = new ArrayDeque<>();
                        Object lastModifiedEntry = null;
                        final ArrayList<ENTRY> entryValueCopy = new ArrayList<>();
                        int maxConsistencyChecks;
                        int iterationErrorCounter;
                        String note;

                        mainLoop:
                        while (true) {

                            // do not burn cpu
                            Thread.yield();

                            if (isSandbox() && Thread.currentThread().isInterrupted()) {
                                throw new InvalidStateException("Cancel check because " + getName() + " shutdown detected!");
                            }

                            // init next iteration
                            iterationCounter++;

                            // handle handler interference
                            maxConsistencyChecks = consistencyHandlerList.size() * entryMap.size() * 2;
                            if (iterationCounter > maxConsistencyChecks) {
                                final int stackSize = MultiException.size(exceptionStack);
                                MultiException.checkAndThrow(() -> stackSize + " error" + (stackSize == 1 ? "" : "s") + " occurred during processing!", exceptionStack);
                                throw new InvalidStateException("ConsistencyHandler" + Arrays.toString(consistencyHandlerQueue.toArray()) + " interference detected!");
                            }

                            // prepare for next iteration
                            if (exceptionStack != null) {
                                iterationErrorCounter = exceptionStack.size();
                                previousExceptionStack = new ExceptionStack(exceptionStack);
                                exceptionStack.clear();
                            } else {
                                iterationErrorCounter = 0;
                            }
                            if (!consistencyHandlerQueue.isEmpty() || iterationErrorCounter != 0) {

                                if (iterationErrorCounter > 0) {
                                    note = " with " + iterationErrorCounter + " errors";
                                } else {
                                    note = "";
                                }

                                if (!consistencyHandlerQueue.isEmpty()) {
                                    note += " after " + consistencyHandlerQueue.size() + " applied modifications";
                                }

                                final int percentage = ((int) (((double) iterationCounter) / ((double) maxConsistencyChecks) * 100));
                                // only print progress information if more than 10% of the max tests are already performed to reduce logger load during unit tests.
                                if (percentage > 10) {
                                    consistencyFeedbackEventFilter.trigger(percentage + "% of max consistency checks passed of " + this + note + ".");
                                }
                            }
                            consistencyHandlerQueue.clear();

                            // consistency check
                            try {
                                for (ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler : consistencyHandlerList) {
                                    consistencyHandler.reset();
                                    entryValueCopy.clear();
                                    entryValueCopy.addAll(new ArrayList<>(entryMap.values()));
                                    for (ENTRY entry : entryValueCopy) {
                                        try {
                                            consistencyHandler.processData(entry.getId(), entry, entryMap, (REGISTRY) this);
                                        } catch (CouldNotPerformException | NullPointerException ex) {
                                            logger.debug("Inconsistency detected by ConsistencyHandler[" + consistencyHandler + "] in Entry[" + entry + "]!");
                                            exceptionStack = MultiException.push(consistencyHandler, new VerificationFailedException("Verification of Entry[" + entry + "] failed with " + consistencyHandler + "!", ex), exceptionStack);
                                        }
                                    }
                                }
                            } catch (EntryModification ex) {

                                // check if consistency handler is looping
                                if (ex.getConsistencyHandler() == consistencyHandlerQueue.peekLast() && ex.getEntry().equals(lastModifiedEntry)) {
                                    throw new InvalidStateException("ConsistencyHandler[" + consistencyHandlerQueue.peekLast() + "] is looping over same Entry[" + lastModifiedEntry + "] more than once!");
                                }

                                consistencyHandlerQueue.remove(ex.getConsistencyHandler());
                                consistencyHandlerQueue.offer(ex.getConsistencyHandler());
                                lastModifiedEntry = ex.getEntry();

                                // inform about modifications
                                try {
                                    if (JPService.getProperty(JPVerbose.class).getValue() && !JPService.getProperty(JPTestMode.class).getValue()) {
                                        log("Consistency modification applied: " + ex.getMessage());
                                    } else {
                                        logger.debug("Consistency modification applied: " + ex.getMessage());
                                    }
                                } catch (JPNotAvailableException exx) {
                                    ExceptionPrinter.printHistory(new CouldNotPerformException("JPVerbose property could not be loaded!", exx), logger, LogLevel.WARN);
                                }
                                pluginPool.afterConsistencyModification((ENTRY) ex.getEntry());
                                modificationCounter++;
                                continue;
                            } catch (Throwable ex) {
                                throw ExceptionPrinter.printHistoryAndReturnThrowable(new InvalidStateException("Fatal error occurred during consistency check!", ex), logger);
                            }

                            // has been an error occurred during current run?
                            if (exceptionStack != null && !exceptionStack.isEmpty()) {

                                // has been the same errors occurred since the last run and so no entry fixes applied during this run?
                                if (previousExceptionStack != null && !previousExceptionStack.isEmpty() && exceptionStack.size() == previousExceptionStack.size()) {

                                    for (int i = 0; i < exceptionStack.size(); i++) {

                                        // Check if the error source is not the same.
                                        if (!exceptionStack.get(i).getSource().equals(previousExceptionStack.get(i).getSource())) {
                                            // continue with consistency check
                                            continue mainLoop;
                                        }

                                        // check if the initial cause of the error is not the same.

                                        final String initialCauseMessage = ExceptionProcessor.getInitialCauseMessage(exceptionStack.get(i).getException());
                                        final String previousInitialCauseMessage = ExceptionProcessor.getInitialCauseMessage(previousExceptionStack.get(i).getException());
                                        if (!initialCauseMessage.equals(previousInitialCauseMessage)) {
                                            // continue with consistency check
                                            continue mainLoop;
                                        }
                                    }
                                    final int stackSize = MultiException.size(exceptionStack);
                                    MultiException.checkAndThrow(() -> stackSize + " error" + (stackSize == 1 ? "" : "s") + " occurred during processing!", exceptionStack);
                                }
                                continue;
                            }

                            logger.debug(this + " consistent.");
                            break;
                        }
                        consistent = true;

                        if (modificationCounter > 0 || consistencyFeedbackEventFilter.isTriggered()) {
                            consistencyFeedbackEventFilter.trigger("100% consistency checks passed of " + this + " after " + modificationCounter + " applied modifications.", true);
                        }
                        return modificationCounter;
                    } catch (CouldNotPerformException ex) {
                        consistent = false;
                        try {
                            if (JPService.getProperty(JPForce.class).getValue()) {
                                ExceptionPrinter.printHistory(new CouldNotPerformException("Consistency process of " + this + " aborted after " + modificationCounter + " modifications but transaction passed because registry force mode is enabled!", ex), logger, LogLevel.WARN);
                                return modificationCounter;
                            }
                        } catch (JPServiceException exx) {
                            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", exx), logger);
                        }
                        throw new CouldNotPerformException("Consistency process of " + this + " aborted!", ex);
                    }
                } finally {
                    consistencyFeedbackEventFilter.reset();
                    afterConsistencyCheck();
                }
            } finally {
                consistencyCheckLock.writeLock().unlock();
            }
        } finally {
            unlock();
        }
    }

    /**
     * Can be overwritten for further registry actions scheduled after consistency checks.
     * <p>
     * Don't forget to pass-through the call to the super class. (super.afterConsistencyCheck())
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if any plugin afterConsistencyCheck fails.
     */
    protected void afterConsistencyCheck() throws CouldNotPerformException {
        pluginPool.afterConsistencyCheck();
    }

    protected void finishTransaction() throws CouldNotPerformException {
        try {
            checkConsistency();
            dependingRegistryObservable.notifyObservers(entryMap);
        } catch (CouldNotPerformException ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new FatalImplementationErrorException("Registry consistency check failed but sandbox check was successful!", this, ex), logger, LogLevel.ERROR);
        }
    }

    private void syncSandbox() {
//        if (consistencyCheckLock.isWriteLocked()) {
//            throw new FatalImplementationErrorException("Sync sandbox registry during consistency check[" + consistencyCheckLock.writeLock().isHeldByCurrentThread() + "]", this);
//        }
        registryLock.readLock().lock();
        try {
            sandbox.sync(entryMap);
        } finally {
            registryLock.readLock().unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isConsistent() {
        return consistent;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isReady() {
        return isConsistent() && !isBusy() && !isNotificationInProgress() && registryLock.getReadLockCount() == 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        shutdownInitiated = true;
        try {
            registryLock.writeLock().lock();

            try {
                super.shutdown();
                removeAllDependencies();
                pluginPool.shutdown();
                for (ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler : consistencyHandlerList) {
                    consistencyHandler.shutdown();
                }
                clear();
            } finally {
                registryLock.writeLock().unlock();
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not shutdown " + this, ex, logger);
        }
    }

    /**
     * Method can be used to register any compatible registry plugin for this
     * registry.
     *
     * @param plugin the plugin to register.
     *
     * @throws CouldNotPerformException is thrown in case the plugin could not
     *                                  be registered.
     * @throws InterruptedException     is thrown if the thread is externally
     *                                  interrupted.
     */
    public void registerPlugin(final PLUGIN plugin) throws CouldNotPerformException, InterruptedException {
        try {
            if (plugin == null) {
                throw new NotAvailableException("plugin");
            }
            pluginPool.addPlugin(plugin);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register Plugin[" + plugin + "]", ex);
        }
    }

    /**
     * Method returns the name of this registry. In case the name was never set
     * for this registry the simple class name of the registry class is returned
     * instead where the "Controller" or "Remote" suffix are removed.
     *
     * @return the name of the registry.
     */
    @Override
    public String getName() {
        if (name == null) {
            return getClass().getSimpleName()
                    .replace("Remote", "")
                    .replace("Controller", "");
        }
        return name;
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
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isSandbox() {
        return false;
    }

    /**
     * This method can be used to log registry log messages which are only printed for the origin registry.
     * Info messages of a sandbox instance are redirected to the debug channel.
     *
     * @param message the info message to print as string.
     */
    protected void log(final String message) {
        try {
            Printer.print("[" + getName() + "]: " + message, LogLevelFilter.getFilteredLogLevel(LogLevel.INFO, isSandbox()), logger);
        } catch (final Throwable ex) {
            System.out.println("fallback message: " + message);
        }
    }

    /**
     * This method can be used to log registry log messages which are only printed for the origin registry.
     * Info messages of a sandbox instance are redirected to the debug channel.
     *
     * @param message   the info message to print as string.
     * @param logLevel  the log level to log the message.
     * @param throwable the cause of the message.
     */
    protected void log(final String message, final LogLevel logLevel, final Throwable throwable) {
        Printer.print(message, throwable, LogLevelFilter.getFilteredLogLevel(logLevel, isSandbox()), logger);
    }

    /**
     * This method can be used to log registry log messages which are only printed for the origin registry.
     * Info messages of a sandbox instance are redirected to the debug channel.
     *
     * @param message  the info message to print as string.
     * @param logLevel the log level to log the message.
     */
    protected void log(final String message, final LogLevel logLevel) {
        Printer.print(message, LogLevelFilter.getFilteredLogLevel(logLevel, isSandbox()), logger);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        return getName();
    }

    /**
     * Blocks until this registries write lock and the write locks of all registries this one depends on are acquired.
     *
     * @throws CouldNotPerformException Is thrown if the process gets interrupted. Additionally the interrupt flag is restored.
     */
    protected void lock() throws CouldNotPerformException {
        boolean successfullyLocked;
        try {
            while (true) {
//                if (!this.isSandbox()) {
//                    logger.info("Lock registry [" + this + "]");
//                }
                /* Acquire the write lock first before recursively locking because the set used for it
                 * is the same for different threads. Else while one thread is currently recursively locking
                 * another can call the same method which will return true because the set already contains
                 * this registry. */
                if (registryLock.writeLock().tryLock()) {
                    try {
                        /* The method recursiveTryLockRegistry is disabled for remote registries so that they cannot be locked
                         * externally. So, call the internal method which does the process but is only visible in this
                         * package. */
                        if (this instanceof RemoteRegistry) {
                            successfullyLocked = ((RemoteRegistry) this).internalRecursiveTryLockRegistry(lockedRegistries);
                        } else {
                            successfullyLocked = recursiveTryLockRegistry(lockedRegistries);
                        }

                        if (successfullyLocked) {
                            /*
                             * If all registries could be locked return and increase the lock counter. The counter is necessary
                             * because the recursive method will only lock every registry once to prevent infinite recursion for
                             * dependency loops. */
                            lockCounter++;
                            return;
                        } else {
                            // locking all has failed so unlock the ones that have been acquired
                            unlockRegistries(lockedRegistries);
                        }
                    } finally {
                        registryLock.writeLock().unlock();
                    }
                }

                // sleep for a random time before trying to lock again
                try {
                    long sleepTime = 20 + randomJitter.nextInt(50);
//                    if (!this.isSandbox()) {
//                        logger.info("Could not lock this so sleep for [" + sleepTime + "][" + this + "]");
//                    }
                    Thread.sleep(sleepTime);
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                    if (!shutdownInitiated) {
                        throw new CouldNotPerformException("Could not lock registry because thread was externally interrupted!", ex);
                    }
                }
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not lock registry!", ex);
        }
    }

    private void unlockRegistries(final Set<Registry> lockedRegistries) {
        assert registryLock.writeLock().isHeldByCurrentThread();

        /* Additional write lock because this registry is also part of the lockedRegistries set.
         * So, if this registry would be the first one in the set it would be unlocked first and another
         * thread could already start locking while not everything is unlocked.
         * But by locking this registry additionally the next thread can only try to lock if all registries
         * this one depends on have been unlocked. */
        registryLock.writeLock().lock();
        try {
            for (Registry registry : lockedRegistries) {
                if (registry instanceof RemoteRegistry) {
                    ((RemoteRegistry) registry).internalUnlockRegistry();
                } else {
                    registry.unlockRegistry();
                }
            }
            lockedRegistries.clear();
        } finally {
            registryLock.writeLock().unlock();
        }
    }

    public boolean isBusy() {
        return registryLock.isWriteLocked();
    }

    public boolean isBusyByCurrentThread() {
        return registryLock.isWriteLockedByCurrentThread();
    }

    protected void unlock() {
        assert registryLock.writeLock().isHeldByCurrentThread();

        if (lockCounter > 1) {
            // if the registry has been locked by the same thread multiple times only decrease the counter
            lockCounter--;
        } else {
            // if the counter is at 1 than unlock all registries and decrease the counter to 0
            lockCounter--;
            unlockRegistries(lockedRegistries);
        }
    }

    protected boolean isWriteLockedByCurrentThread() {
        return registryLock.writeLock().isHeldByCurrentThread();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws RejectedException {@inheritDoc}
     */
    @Override
    public boolean tryLockRegistry() throws RejectedException {
        return registryLock.writeLock().tryLock();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws RejectedException {@inheritDoc}
     */
    @Override
    public boolean recursiveTryLockRegistry(final Set<Registry> lockedRegistries) throws RejectedException {
        if (lockedRegistries.contains(this)) {
            /* If this registry is in the set the lock is already acquired, so return true.
             * This prevents infinite recursions for dependency loops but is also the reason why
             * this method should be called while already holding the write lock for the current registry.
             * Because if two threads use the same set, which is the case for the lock method, than one
             * can be in the process and has already acquired the lock for this registry but still needs
             * it for dependencies, the other thread returns true here. */
            return true;
        } else {
            // try to acquire the write lock for this registry
            if (registryLock.writeLock().tryLock()) {
                // the lock has been acquired so add this to the set
                lockedRegistries.add(this);

                dependingRegistryMapLock.readLock().lock();
                try {
                    // iterate over all registries this one depends on and try to lock recursively
                    for (Registry registry : dependingRegistryMap.keySet()) {
                        // if one recursive try lock returns false then return false as well
                        if (registry instanceof RemoteRegistry) {
                            if (!((RemoteRegistry) registry).internalRecursiveTryLockRegistry(lockedRegistries)) {
                                return false;
                            }
                        } else {
                            if (!registry.recursiveTryLockRegistry(lockedRegistries)) {
                                return false;
                            }
                        }
                    }
                } finally {
                    dependingRegistryMapLock.readLock().unlock();
                }

                // all registries this one depends on could be locked recursively as well, so return true
                return true;
            } else {
//                if (!isSandbox()) {
//                    logger.info("Failed to lock " + this);
//                }
                // acquiring the write lock for this registry failed, so return false
                return false;
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void unlockRegistry() {
        assert registryLock.writeLock().isHeldByCurrentThread();
        registryLock.writeLock().unlock();
    }

    @Override
    public void addDependencyObserver(final Observer<Registry<KEY, ENTRY>, Map<KEY, ENTRY>> observer) {
        dependingRegistryObservable.addObserver(observer);
    }

    @Override
    public void removeDependencyObserver(final Observer<Registry<KEY, ENTRY>, Map<KEY, ENTRY>> observer) {
        dependingRegistryObservable.removeObserver(observer);
    }

    private void notifySuccessfulTransaction() {
        try {
            transactionPerformedNotifier.notifyChange();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not notify all transation listeners.", ex, logger);
        } catch (InterruptedException ex) {
            // recover interruption
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Registers the given listener to get informed about performed transactions.
     *
     * @param listener the listener to register.
     */
    public void addTransactionListener(final ChangeListener listener) {
        transactionPerformedNotifier.addlistener(listener);
    }

    /**
     * Removes an already registered transaction listener.
     *
     * @param listener the listener to remove.
     */
    public void removeTransactionListener(final ChangeListener listener) {
        transactionPerformedNotifier.removeListener(listener);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    public boolean isShutdownInitiated() {
        return shutdownInitiated;
    }

    private class DependencyConsistencyCheckTrigger implements Observer<Registry<KEY, ENTRY>, Map<KEY, ENTRY>>, Shutdownable, Activatable {

        private final Registry dependency;
        private boolean active;

        public DependencyConsistencyCheckTrigger(final Registry dependency) {
            this.dependency = dependency;
            this.active = false;
        }


        /**
         * {@inheritDoc}
         *
         * @param source {@inheritDoc}
         * @param data   {@inheritDoc}
         *
         * @throws Exception {@inheritDoc}
         */
        @Override
        public void update(Registry<KEY, ENTRY> source, Map<KEY, ENTRY> data) throws Exception {
            //TODO: update on sandbox level should be handled first
            try {
                if (dependency.isConsistent()) {
                    boolean notificationNeeded;
                    lock();
                    try {
                        try {
                            pluginPool.beforeUpstreamDependencyNotification(dependency);
                            notificationNeeded = checkConsistency() > 0 || notificationSkipped;
                            if (notificationNeeded) {
                                dependingRegistryObservable.notifyObservers(entryMap);
                            }
                        } finally {
                            syncSandbox();
                        }
                    } finally {
                        unlock();
                    }

                    if (notificationNeeded) {
                        notifyObservers();
                    }
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Registry inconsistent after change of depending " + source + " change.", ex, logger);
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void shutdown() {
            try {
                deactivate();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not shutdown " + this, ex, logger);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }

        @Override
        public void activate() throws CouldNotPerformException, InterruptedException {
            active = true;
            dependency.addDependencyObserver(this);
        }

        @Override
        public void deactivate() throws CouldNotPerformException, InterruptedException {
            dependency.removeDependencyObserver(this);
            active = false;
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }
}
