package org.openbase.jul.storage.registry.plugin;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.Initializable;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.storage.registry.Registry;

import java.io.File;

/**
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * @param <KEY>
 * @param <ENTRY>
 */
public interface RegistryPlugin<KEY, ENTRY extends Identifiable<KEY>, REGISTRY extends Registry<KEY, ENTRY>> extends Initializable<REGISTRY>, Shutdownable {

    void prepareRegistry(final File registyDirectory) throws CouldNotPerformException;

    void beforeRegister(final ENTRY entry) throws RejectedException;

    void afterRegister(final ENTRY entry) throws CouldNotPerformException;

    void beforeUpdate(final ENTRY entry) throws RejectedException;

    /**
     * Method is called after each updated entry.
     * Be aware that this method can be called multible times for the same update in case the entry is modified during the consistency check.
     *
     * @param entry the updated entry.
     *
     * @throws CouldNotPerformException can be thrown in case something went wrong during the plugin routine.
     */
    void afterUpdate(final ENTRY entry) throws CouldNotPerformException;

    void beforeRemove(final ENTRY entry) throws RejectedException;

    void afterRemove(final ENTRY entry) throws CouldNotPerformException;

    void afterConsistencyModification(final ENTRY entry) throws CouldNotPerformException;

    void afterRegistryChange() throws CouldNotPerformException;

    void beforeConsistencyCheck() throws CouldNotPerformException;

    void afterConsistencyCheck() throws CouldNotPerformException;

    void beforeClear() throws CouldNotPerformException;

    void beforeGet(final KEY key) throws RejectedException;

    void checkAccess() throws RejectedException;

    void beforeUpstreamDependencyNotification(final Registry dependency) throws CouldNotPerformException;

    @Override
    void init(REGISTRY registry) throws InitializationException, InterruptedException;

}
