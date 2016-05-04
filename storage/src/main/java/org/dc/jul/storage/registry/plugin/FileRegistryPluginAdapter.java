package org.dc.jul.storage.registry.plugin;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.storage.file.FileSynchronizer;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public abstract class FileRegistryPluginAdapter<KEY, ENTRY extends Identifiable<KEY>> extends RegistryPluginAdapter<KEY, ENTRY> implements FileRegistryPlugin<KEY, ENTRY> {

    @Override
    public void beforeRegister(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
    }

    @Override
    public void afterRegister(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeRemove(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
    }

    @Override
    public void afterRemove(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeUpdate(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
    }

    @Override
    public void afterUpdate(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeGet(KEY key, FileSynchronizer fileSynchronizer) throws RejectedException {
    }
}
