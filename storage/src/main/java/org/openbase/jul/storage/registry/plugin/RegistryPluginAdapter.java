package org.openbase.jul.storage.registry.plugin;

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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.RejectedException;
import org.openbase.jul.iface.Identifiable;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public abstract class RegistryPluginAdapter<KEY, ENTRY extends Identifiable<KEY>> implements RegistryPlugin<KEY, ENTRY> {

    @Override
    public void beforeRegister(ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterRegister(ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeUpdate(ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterUpdate(ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeRemove(ENTRY entry) throws RejectedException {
    }

    @Override
    public void afterRemove(ENTRY entry) throws CouldNotPerformException {
    }

    @Override
    public void beforeClear() throws CouldNotPerformException {
    }

    @Override
    public void beforeGet(KEY key) throws RejectedException {
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
}
