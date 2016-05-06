package org.dc.jul.pattern;

/*
 * #%L
 * JUL Pattern
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

import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Manageable;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <ID> Identifier
 * @param <M> Message
 * @param <CONFIG> Configuration
 */
public interface ConfigurableRemote<ID, M, CONFIG> extends IdentifiableRemote<ID, M>, Manageable<CONFIG> {

    public CONFIG getConfig() throws NotAvailableException;
    
    
    /**
     * This method allows the registration of config observers to get informed about config updates.
     *
     * @param observer
     */
    public void addConfigObserver(final Observer<CONFIG> observer);

    /**
     * This method removes already registered config observers.
     *
     * @param observer
     */
    public void removeConfigObserver(final Observer<CONFIG> observer);
}
