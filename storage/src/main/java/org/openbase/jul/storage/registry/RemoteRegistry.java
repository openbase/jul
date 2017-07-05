package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import com.google.protobuf.GeneratedMessage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.NotSupportedException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import static org.openbase.jul.iface.Identifiable.TYPE_FIELD_ID;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.jul.storage.registry.plugin.RemoteRegistryPlugin;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class RemoteRegistry<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractRegistry<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, ProtoBufRegistry<KEY, M, MB>, RemoteRegistryPlugin<KEY, IdentifiableMessage<KEY, M, MB>>> implements ProtoBufRegistry<KEY, M, MB> {

    /**
     * An optional configurable registryRemote where this remote is than bound to.
     * If configured this registryRemote is used for registry state checks and synchronization issues.
     *
     * Note: The message type {@code M} of the RegistryRemote can be different from the message type {@code M} of this class. Thats why it is marked as unknown.
     */
    private RegistryRemote<?> registryRemote;

    public RemoteRegistry() throws InstantiationException {
        this(new HashMap<>());
    }

    public RemoteRegistry(final Map<KEY, IdentifiableMessage<KEY, M, MB>> internalMap) throws InstantiationException {
        super(internalMap);
    }

    public synchronized void notifyRegistryUpdate(final Collection<M> values) throws CouldNotPerformException {
        Map<KEY, IdentifiableMessage<KEY, M, MB>> newRegistryMap = new HashMap<>();
        for (M value : values) {
            IdentifiableMessage<KEY, M, MB> data = new IdentifiableMessage<>(value);
            newRegistryMap.put(data.getId(), data);
        }
        replaceInternalMap(newRegistryMap);
    }

    public KEY getId(final M entry) throws CouldNotPerformException {
        KEY key = (KEY) entry.getField(entry.getDescriptorForType().findFieldByName(TYPE_FIELD_ID));
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
    public boolean contains(final M entry) throws CouldNotPerformException {
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
    public void registerConsistencyHandler(final ConsistencyHandler<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, final ProtoBufRegistry<KEY, M, MB>> consistencyHandler) throws CouldNotPerformException {
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

    @Override
    public void unlockRegistry() {
        // because remote registry does not support locks there is no need for any action here.
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
     *
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller.
     * So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @throws InterruptedException is thrown in case the thread was externally interrupted.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown if the wait could not be performed.
     */
    public void waitUntilReady() throws InterruptedException, CouldNotPerformException {
        try {
            waitUntilReadyFuture().get();
        } catch (final ExecutionException | CancellationException ex) {
            throw new CouldNotPerformException("Could not wait unit registry is ready.", ex);
        }
    }

    /**
     * Method blocks until the registry is not handling any tasks and is currently consistent.
     *
     * Note: If you have just modified the registry this method can maybe return immediately if the task is not yet received by the registry controller.
     * So you should prefer the futures of the modification methods for synchronization tasks.
     *
     * @return a future which is finished if the registry is ready.
     */
    public Future<Void> waitUntilReadyFuture() {
        try {
            return getRegistryRemote().waitUntilReadyFuture();
        } catch (NotAvailableException ex) {
            return FutureProcessor.canceledFuture(null, ex);
        }
    }

    // todo: Protect this method because external manipulation would be really bad.
    public void setRegistryRemote(final RegistryRemote<?> remote) {
        this.registryRemote = remote;
    }

    /**
     * Method returns the registry remote where this instance is bound to.
     * @return the registry remote if available.
     * @throws NotAvailableException Because this bound is optionally this method may throws an {@code  NotAvailableException} in case the remote registry is independent of any registry remote.
     */
    protected RegistryRemote<?> getRegistryRemote() throws NotAvailableException {
        if (registryRemote == null) {
            throw new NotAvailableException("RegistryRemote");
        }
        return registryRemote;
    }
}
