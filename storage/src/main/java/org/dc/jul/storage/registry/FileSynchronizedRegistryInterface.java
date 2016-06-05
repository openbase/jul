package org.dc.jul.storage.registry;

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
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Identifiable;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <R>
 */
public interface FileSynchronizedRegistryInterface<KEY, VALUE extends Identifiable<KEY>, R extends FileSynchronizedRegistryInterface<KEY, VALUE, R>> extends Registry<KEY, VALUE, R> {

    public void loadRegistry() throws CouldNotPerformException;

    public void saveRegistry() throws CouldNotPerformException;
    
    public Integer getDBVersion() throws NotAvailableException;;
}
