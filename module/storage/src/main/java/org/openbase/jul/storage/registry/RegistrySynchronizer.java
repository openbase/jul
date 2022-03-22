package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program. If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

import com.google.protobuf.AbstractMessage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.iface.Configurable;
import org.openbase.jul.pattern.Factory;
import org.openbase.jul.pattern.Filter;

import java.util.ArrayList;
import java.util.List;

import static org.openbase.jul.iface.Identifiable.TYPE_FIELD_ID;

/**
 * @param <KEY>
 * @param <ENTRY>
 * @param <CONFIG_M>
 * @param <CONFIG_MB>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RegistrySynchronizer<KEY, ENTRY extends Configurable<KEY, CONFIG_M>, CONFIG_M extends AbstractMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> extends AbstractSynchronizer<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>> {

    protected final SynchronizableRegistry<KEY, ENTRY> localRegistry;
    private final Factory<ENTRY, CONFIG_M> factory;
    protected final RemoteRegistry<KEY, CONFIG_M, CONFIG_MB> remoteRegistry;
    private final List<Filter<CONFIG_M>> filterList;


    public RegistrySynchronizer(final SynchronizableRegistry<KEY, ENTRY> localRegistry, final RemoteRegistry<KEY, CONFIG_M, CONFIG_MB> remoteRegistry, final RegistryRemote registryRemote, final Factory<ENTRY, CONFIG_M> factory) throws org.openbase.jul.exception.InstantiationException {
        super(registryRemote);
        this.localRegistry = localRegistry;
        this.remoteRegistry = remoteRegistry;
        this.factory = factory;
        this.filterList = new ArrayList<>();
    }

    @Override
    public void shutdown() {
        super.shutdown();
        synchronized (synchronizationLock) {
            localRegistry.shutdown();
        }
    }

    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = localRegistry.get(remoteRegistry.getId(config));
        entry.applyConfigUpdate(config);

        // entry is already updated but a local registry update is needed to trigger depending observers of the local registry.
        localRegistry.update(entry);
        return entry;
    }

    public ENTRY remove(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return localRegistry.remove(getId(config));
    }

    public KEY getId(final CONFIG_M entry) throws CouldNotPerformException {
        KEY key = (KEY) entry.getField(entry.getDescriptorForType().findFieldByName(TYPE_FIELD_ID));
        if (!localRegistry.contains(key)) {
            throw new CouldNotPerformException("Entry for given Key[" + key + "] is not available for local registry!");
        }
        return key;
    }

    @Override
    public void update(IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB> identifiableMessage) throws CouldNotPerformException, InterruptedException {
        update(identifiableMessage.getMessage());
    }

    @Override
    public void register(IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB> identifiableMessage) throws CouldNotPerformException, InterruptedException {
        register(identifiableMessage.getMessage());
    }

    @Override
    public void remove(IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB> identifiableMessage) throws CouldNotPerformException, InterruptedException {
        remove(identifiableMessage.getMessage());
    }

    public ENTRY register(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return localRegistry.register(factory.newInstance(config));
    }

    @Override
    public List<IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>> getEntries() throws CouldNotPerformException {
        return remoteRegistry.getEntries();
    }

    @Override
    public boolean isSupported(IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB> identifiableMessage) {
        for (Filter<CONFIG_M> filter : filterList) {
            if (filter.match(identifiableMessage.getMessage())) {
                return false;
            }
        }
        return true;
    }

    @Override
    protected void afterInternalSync() {
        localRegistry.notifySynchronization();
    }

    public boolean addFilter(final Filter<CONFIG_M> filter) {
        return filterList.add(filter);
    }

    public boolean removeFilter(final Filter<CONFIG_M> filter) {
        return filterList.remove(filter);
    }
}
