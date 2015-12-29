/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.iface.Activatable;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.pattern.Factory;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <CONFIG_M>
 * @param <CONFIG_MB>
 */
public abstract class ActivatableEntryRegistrySynchronizer<KEY, ENTRY extends Identifiable<KEY> & Activatable, CONFIG_M extends GeneratedMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> extends RegistrySynchronizer<KEY, ENTRY, CONFIG_M, CONFIG_MB> {

    public ActivatableEntryRegistrySynchronizer(Registry<KEY, ENTRY> registry, RemoteRegistry<KEY, CONFIG_M, CONFIG_MB, ?> remoteRegistry, Factory<ENTRY, CONFIG_M> factory) throws InstantiationException {
        super(registry, remoteRegistry, factory);
    }

    @Override
    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = super.update(config);
        if (activationCondition(config)) {
            entry.activate();
        } else {
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
        return entry;
    }

    public abstract boolean activationCondition(final CONFIG_M config);
}
