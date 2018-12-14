package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import com.google.protobuf.Message;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Configurable;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.pattern.Factory;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <CONFIG_M>
 * @param <CONFIG_MB>
 */
public abstract class ActivatableEntryRegistrySynchronizer<KEY, ENTRY extends Configurable<KEY, CONFIG_M> & Activatable & Shutdownable, CONFIG_M extends AbstractMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> extends RegistrySynchronizer<KEY, ENTRY, CONFIG_M, CONFIG_MB> {

    public ActivatableEntryRegistrySynchronizer(SynchronizableRegistry<KEY, ENTRY> localRegistry, RemoteRegistry<KEY, CONFIG_M, CONFIG_MB> remoteRegistry, final RegistryRemote registryRemote, Factory<ENTRY, CONFIG_M> factory) throws org.openbase.jul.exception.InstantiationException {
        super(localRegistry, remoteRegistry, registryRemote, factory);
    }

    @Override
    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.update(config);
        if (activationCondition(config) && !entry.isActive()) {
            entry.activate();
        } else if (!activationCondition(config) && entry.isActive()) {
            entry.deactivate();
        }
        return entry;
    }

    @Override
    public ENTRY register(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.register(config);
        if (activationCondition(config)) {
            entry.activate();
        }
        return entry;
    }

    @Override
    public ENTRY remove(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.remove(config);
        entry.deactivate();
        entry.shutdown();
        return entry;
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        super.deactivate();

        for (ENTRY entry : localRegistry.getEntries()) {
            entry.deactivate();
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        for (ENTRY entry : localRegistry.getEntries()) {
            if (activationCondition(entry.getConfig())) {
                entry.activate();
            }
        }
        super.activate();
    }

    @Override
    public void shutdown() {
        try {
            for (ENTRY entry : localRegistry.getEntries()) {
                entry.shutdown();
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger);
        }
        super.shutdown();
    }

    public abstract boolean activationCondition(final CONFIG_M config);
}
