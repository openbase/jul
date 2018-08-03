package org.openbase.jul.storage.registry.plugin;

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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.storage.registry.Registry;

import java.util.HashSet;
import java.util.Set;

/**
 * @param <KEY>
 * @param <ENTRY>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractRegistryPluginAdapter<KEY, ENTRY extends Identifiable<KEY>, REGISTRY extends Registry<KEY, ENTRY>> implements RegistryPlugin<KEY, ENTRY, REGISTRY> {

    private REGISTRY registry;
    private final Set<ENTRY> changedEntryList = new HashSet<>();

    @Override
    public void init(final REGISTRY registry) throws InitializationException, InterruptedException {
        try {
            if (this.registry != null) {
                throw new InvalidStateException("Plugin already initialized!");
            }
            this.registry = registry;
        } catch (final CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public REGISTRY getRegistry() {
        return registry;
    }

    @Override
    public void beforeRegister(final ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterRegister(final ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeUpdate(final ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterUpdate(final ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeRemove(final ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterRemove(final ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeClear() throws CouldNotPerformException {
    }

    @Override
    public void beforeGet(final KEY key) throws RejectedException {
    }

    @Override
    public void beforeGetEntries() throws CouldNotPerformException {
    }

    @Override
    public void checkAccess() throws RejectedException {
    }

    @Override
    public void shutdown() {
    }

    @Override
    public void afterRegistryChange() throws CouldNotPerformException {
    }

    @Override
    public void beforeConsistencyCheck() throws RejectedException {
    }

    /**
     * Method triggers entry updates of all entries which was been modified during the last consistency check.
     * <p>
     * Note: do not register new entries within this method because consistency checks will be skipped.
     *
     * @throws CouldNotPerformException
     */
    @Override
    public void afterConsistencyCheck() throws CouldNotPerformException {
        for (final ENTRY entry : changedEntryList) {
            afterUpdate(entry);
        }
        changedEntryList.clear();
    }

    @Override
    public void afterConsistencyModification(ENTRY entry) throws CouldNotPerformException {
        changedEntryList.add(entry);
    }

    @Override
    public void beforeUpstreamDependencyNotification(Registry dependency) throws CouldNotPerformException {
    }
}
