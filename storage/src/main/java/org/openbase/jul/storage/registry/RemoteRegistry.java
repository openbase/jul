package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.NotSupportedException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import static org.openbase.jul.iface.Identifiable.TYPE_FIELD_ID;
import org.openbase.jul.storage.registry.plugin.RemoteRegistryPlugin;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class RemoteRegistry<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractRegistry<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, ProtoBufRegistry<KEY, M, MB>, RemoteRegistryPlugin<KEY, IdentifiableMessage<KEY, M, MB>>> implements ProtoBufRegistry<KEY, M, MB> {

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
    public void registerConsistencyHandler(ConsistencyHandler<KEY, IdentifiableMessage<KEY, M, MB>, Map<KEY, IdentifiableMessage<KEY, M, MB>>, ProtoBufRegistry<KEY, M, MB>> consistencyHandler) throws CouldNotPerformException {
        throw new NotSupportedException("registerConsistencyHandler", "method", this);
    }

    @Override
    public boolean lockRegistry() throws RejectedException {
        throw new RejectedException("RemoteRegistry not lockable!");
    }

    @Override
    public void unlockRegistry() {
        // because remote registry does not support locks there is no need for any action here.
    }
}
