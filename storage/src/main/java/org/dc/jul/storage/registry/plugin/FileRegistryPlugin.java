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
 * @author <a href="mailto:MarianPohling@cit-ec.uni-bielefeld.de">mpohling</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public interface FileRegistryPlugin<KEY, ENTRY extends Identifiable<KEY>> extends RegistryPlugin<KEY, ENTRY> {

    public void beforeRegister(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws RejectedException;

    public void afterRegister(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeRemove(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws RejectedException;

    public void afterRemove(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeUpdate(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws RejectedException;

    public void afterUpdate(final ENTRY entry, final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeGet(final KEY key, final FileSynchronizer fileSynchronizer) throws RejectedException;

}
