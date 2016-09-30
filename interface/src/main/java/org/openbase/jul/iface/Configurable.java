package org.openbase.jul.iface;

/*
 * #%L
 * JUL Interface
 * $Id:$
 * $HeadURL:$
 *
 %%
 Copyright (C) 2015 - 2016 openbase.org
 *
 %%
 This program is free software: you can redistribute it and/or modify
 it under the terms of the GNU Lesser General Public License as
 published by the Free Software Foundation, either version 3 of the
 License, or (at your option) any later version.
 
 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Lesser Public License for more details.
 
 You should have received a copy of the GNU General Lesser Public
 License along with this program.  If not, see
 <http://www.gnu.org/licenses/lgpl-3.0.html>.
 #L%
 */

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.provider.ConfigProvider;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * @param <ID> The id type of the configurable instance.
 * @param <CONFIG> The configuration type.
 */
public interface Configurable<ID, CONFIG> extends Identifiable<ID>, ConfigProvider<CONFIG> {

    /**
     * Method can be used to update the internal configuration.
     * @param config
     * @return
     * @throws CouldNotPerformException
     * @throws InterruptedException 
     */
    CONFIG applyConfigUpdate(final CONFIG config) throws CouldNotPerformException, InterruptedException;
    
}
