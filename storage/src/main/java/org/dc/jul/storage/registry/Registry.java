/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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

import java.util.List;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.iface.Writable;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 * @param <R>
 */
public interface Registry<KEY, ENTRY extends Identifiable<KEY>, R extends Registry<KEY, ENTRY, R>> extends Writable {

    public String getName();

    public ENTRY register(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY update(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY remove(final KEY key) throws CouldNotPerformException;

    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY get(final KEY key) throws CouldNotPerformException;

    public List<ENTRY> getEntries() throws CouldNotPerformException;

    public boolean contains(final ENTRY entry) throws CouldNotPerformException;

    public boolean contains(final KEY key) throws CouldNotPerformException;

    public void clear() throws CouldNotPerformException;

    public int size();

    public boolean isReadOnly();

    public boolean isConsistent();

}
