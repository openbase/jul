/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
import org.dc.jul.iface.Initializable;
import org.dc.jul.iface.Shutdownable;
import org.dc.jul.storage.registry.Registry;

/**
 *
 * @author <a href="mailto:MarianPohling@cit-ec.uni-bielefeld.de">mpohling</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public interface RegistryPlugin<KEY, ENTRY extends Identifiable<KEY>> extends Initializable<Registry<KEY, ENTRY, ?>>, Shutdownable {

    public void beforeRegister(final ENTRY entry) throws RejectedException;

    public void afterRegister(final ENTRY entry) throws CouldNotPerformException;

    public void beforeUpdate(final ENTRY entry) throws RejectedException;

    public void afterUpdate(final ENTRY entry) throws CouldNotPerformException;

    public void beforeRemove(final ENTRY entry) throws RejectedException;

    public void afterRemove(final ENTRY entry) throws CouldNotPerformException;

    public void beforeClear() throws CouldNotPerformException;

    public void beforeGet(final KEY key) throws RejectedException;

    public void beforeGetEntries() throws CouldNotPerformException;

    public void checkAccess() throws RejectedException;
}
