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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.iface.Configurable;
import org.openbase.jul.iface.Enableable;
import org.openbase.jul.pattern.Factory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * @param <KEY>
 * @param <ENTRY>
 * @param <CONFIG_M>
 * @param <CONFIG_MB>
 */
public abstract class EnableableEntryRegistrySynchronizer<KEY, ENTRY extends Configurable<KEY, CONFIG_M> & Enableable, CONFIG_M extends GeneratedMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> extends RegistrySynchronizer<KEY, ENTRY, CONFIG_M, CONFIG_MB> {

    public EnableableEntryRegistrySynchronizer(RegistryImpl<KEY, ENTRY> registry, RemoteRegistry<KEY, CONFIG_M, CONFIG_MB> remoteRegistry, Factory<ENTRY, CONFIG_M> factory) throws InstantiationException {
        super(registry, remoteRegistry, factory);
    }

    @Override
    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.update(config);
        if (enablingCondition(config)) {
            entry.enable();
        } else {
            entry.disable();
        }
        return entry;
    }

    @Override
    public ENTRY register(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.register(config);
        if (enablingCondition(config)) {
            entry.enable();
        }
        return entry;
    }

    @Override
    public ENTRY remove(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.remove(config);
        entry.disable();
        return entry;
    }

    public abstract boolean enablingCondition(final CONFIG_M config);
}
