package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import com.google.protobuf.AbstractMessage;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.IdentifiableMessageMap;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.jul.storage.registry.plugin.RemoteRegistryPlugin;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.*;

import static org.openbase.jul.iface.Identifiable.TYPE_FIELD_ID;

/**
 * @param <KEY>
 * @param <M>
 * @param <MB>
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RemoteRegistry<KEY, M extends AbstractMessage, MB extends M.Builder<MB>> extends AbstractRegistry<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, ProtoBufRegistry<KEY, M, MB>, RemoteRegistryPlugin<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufRegistry<KEY, M, MB>>> implements ProtoBufRegistry<KEY, M, MB>, DataProvider<Map<KEY, IdentifiableMessage<KEY, M, MB>>> {

    /**
     * An optional configurable registryRemote where this remote is than bound to.
     * If configured this registryRemote is used for registry state checks and synchronization issues.
     * <p>
     * Note: The message type {@code M} of the RegistryRemote can be different from the message type {@code M} of this class. Thats why it is marked as unknown.
     */
    private final RegistryRemote<?> registryRemote;

    public RemoteRegistry() throws InstantiationException {
        this(null, new HashMap<>());
    }

    public RemoteRegistry(final Map<KEY, IdentifiableMessage<KEY, M, MB>> internalMap) throws InstantiationException {
        this(null, internalMap);
    }

    public RemoteRegistry(final RegistryRemote<?> registryRemote) throws InstantiationException {
        this(registryRemote, new HashMap<>());
    }

    public RemoteRegistry(final RegistryRemote<?> registryRemote, final Map<KEY, IdentifiableMessage<KEY, M, MB>> internalMap) throws InstantiationException {
        super(internalMap);
        this.registryRemote = registryRemote;
    }

    public synchronized void notifyRegistryUpdate(final Collection<M> values) throws CouldNotPerformException {
        replaceInternalMap(new IdentifiableMessageMap<>(values));
    }

    public KEY getId(final M entry) throws CouldNotPerformException {
        final KEY key = (KEY) entry.getField(entry.getDescriptorForType().findFieldByName(TYPE_FIELD_ID));
        if (!contains(key)) {
            throw new CouldNotPerformException("Entry for given Key[" + key + "] is not available!");
        }
        return key;
    }

    @Override
    public M getMessage(final KEY key) throws CouldNotPerformException {
        return get(key).getMessage();
    }

    @Override
    public MB getBuilder(final KEY key) throws CouldNotPerformException {
        return (MB) getMessage(key).toBuilder();
    }

    @Override
    public M register(final M entry) throws CouldNotPerformException {
        throw new NotSupportedException("register", this, "Operation not permitted!");
    }

    @Override
    public M update(final M entry) throws CouldNotPerformException {
        throw new NotSupportedException("update", this, "Operation not permitted!");
    }

    @Override
    public M remove(final M entry) throws CouldNotPerformException {
        throw new NotSupportedException("remove", this, "Operation not permitted!");
    }

    @Override
    public boolean contains(final M entry) {
        KEY key;
        try {
            key = getId(entry);
        } catch (CouldNotPerformException ex) {
            return false;
        }
        return super.contains(key);
    }

    @Override
    public void loadRegistry() throws CouldNotPerformException {
        throw new NotSupportedException("loadRegistry", this, "Operation not permitted!");
    }

    @Override
    public void saveRegistry() throws CouldNotPerformException {
        throw new NotSupportedException("saveRegistry", this, "Operation not permitted!");
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        List<M> messageList = new ArrayList<>();
        for (IdentifiableMessage<KEY, M, MB> messageContainer : getEntries()) {
            messageList.add(messageContainer.getMessage());
        }
        return messageList;
    }

    @Override
    public Integer getDBVersion() throws NotAvailableException {
        //TODO mpohling: implement!
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isLocalRegistry() {
        //TODO mpohling: implement!
        return false;
    }

    @Override
    public File getDatabaseDirectory() throws NotAvailableException {
        throw new NotAvailableException("DatabaseDirectory", new NotSupportedException("A remote registry do not provide a database directory!", this));
    }

    @Override
    public void registerConsistencyHandler(final ConsistencyHandler<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, ProtoBufRegistry<KEY, M, MB>> consistencyHandler) throws CouldNotPerformException {
        throw new NotSupportedException("registerConsistencyHandler", this);
    }

    @Override
    public void registerDependency(final Registry registry) throws CouldNotPerformException {
        throw new NotSupportedException("registerDependency", this);
    }

    @Override
    public boolean tryLockRegistry() throws RejectedException {
        throw new RejectedException("RemoteRegistry not lockable!");
    }

    boolean internalTryLockRegistry() throws RejectedException {
        return super.tryLockRegistry();
    }

    @Override
    public boolean recursiveTryLockRegistry(Set<Registry> lockedRegistries) throws RejectedException {
        throw new RejectedException("RemoteRegistry not externally lockable!");
    }

    boolean internalRecursiveTryLockRegistry(final Set<Registry> lockedRegistries) throws RejectedException {
        return super.recursiveTryLockRegistry(lockedRegistries);
    }

    @Override
    public void unlockRegistry() {
        // because remote registry does not support locks there is no need for any action here.
    }

    void internalUnlockRegistry() {
        super.unlockRegistry();
    }

    @Override
    public boolean isReadOnly() {
        if (registryRemote != null && !registryRemote.isConnected()) {
            return true;
        }
        return super.isReadOnly();
    }

    /**
     * Method blocks until the referred registry is not handling any tasks and is currently consistent.
     * <p>
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller.
     * So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @throws InterruptedException                                is thrown in case the thread was externally interrupted.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if the wait could not be performed.
     */
    public void waitUntilReady() throws InterruptedException, CouldNotPerformException {
        try {
            while (true) {
                try {
                    waitUntilReadyFuture().get((JPService.testMode() ? 5 : 300), TimeUnit.SECONDS);
                    break;
                } catch (final TimeoutException ex) {
                    logger.warn("Still waiting for " + getName());
                    continue;
                }
            }
        } catch (final ExecutionException | CancellationException ex) {
            throw new CouldNotPerformException("Could not wait until " + getName() + " is ready.", ex);
        }
    }

    /**
     * Method blocks until the registry is not handling any tasks and is currently consistent.
     * <p>
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller.
     * So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @return a future which is finished if the registry is ready.
     */
    public Future<Void> waitUntilReadyFuture() {
        try {
            return getRegistryRemote().waitUntilReadyFuture();
        } catch (NotAvailableException ex) {
            return FutureProcessor.canceledFuture(Void.class, ex);
        }
    }

    @Override
    public Class<Map<KEY, IdentifiableMessage<KEY, M, MB>>> getDataClass() {
        return getEntryMapClass();
    }

    @Override
    public Map<KEY, IdentifiableMessage<KEY, M, MB>> getData() throws NotAvailableException {
        return getValue();
    }

    @Override
    public Future<Map<KEY, IdentifiableMessage<KEY, M, MB>>> getDataFuture() {
        return getValueFuture();
    }


    @Override
    public void addDataObserver(final Observer<DataProvider<Map<KEY, IdentifiableMessage<KEY, M, MB>>>, Map<KEY, IdentifiableMessage<KEY, M, MB>>> observer) {
        addObserver(observer);
    }

    @Override
    public void removeDataObserver(final Observer<DataProvider<Map<KEY, IdentifiableMessage<KEY, M, MB>>>, Map<KEY, IdentifiableMessage<KEY, M, MB>>> observer) {
        removeObserver(observer);
    }

    /**
     * Method blocks until the remote registry is synchronized.
     *
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    public void waitForData() throws InterruptedException {
        try {
            if (registryRemote == null) {
                waitForValue();
                return;
            }
            getRegistryRemote().waitForData();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not wait until " + getName() + " is ready!", ex, logger);
        }
    }

    @Override
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        try {
            if (registryRemote == null) {
                waitForValue(timeout, timeUnit);
                return;
            }
            getRegistryRemote().waitForData(timeout, timeUnit);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not wait until " + getName() + " is is ready!", ex, logger);
        }
    }

    /**
     * Method returns the registry remote where this instance is bound to.
     *
     * @return the registry remote if available.
     *
     * @throws NotAvailableException Because this bound is optionally this method may throws an {@code  NotAvailableException} in case the remote registry is independent of any registry remote.
     */
    protected RegistryRemote<?> getRegistryRemote() throws NotAvailableException {
        if (registryRemote == null) {
            throw new NotAvailableException("RegistryRemote");
        }
        return registryRemote;
    }

    /**
     * Check if the remote registry provides already valid data.
     *
     * @return if data is available
     */
    @Override
    public boolean isDataAvailable() {
        if (registryRemote == null) {
            return isValueAvailable();
        }
        return registryRemote.isDataAvailable();
    }

    @Override
    public void validateData() throws InvalidStateException {
        registryRemote.validateData();

        if (isDataAvailable()) {
            throw new InvalidStateException(new NotAvailableException("Data"));
        }
    }
}
