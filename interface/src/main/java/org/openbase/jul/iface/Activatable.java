package org.openbase.jul.iface;

/*
 * #%L
 * JUL Interface
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

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface Activatable {

    /**
     * This method activates this instance.
     * 
     * Needed resources will be allocated and communication instances instantiated and started.
     *
     * @throws CouldNotPerformException is thrown in case the instance could not be activated.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
	public void activate() throws CouldNotPerformException, InterruptedException;

    /**
     * This method deactivates this instance.
     * 
     * Owned resources will be released and communication instances will be stopped.
     *
     * @throws CouldNotPerformException is thrown in case the instance could not be deactivated.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
	public void deactivate() throws CouldNotPerformException, InterruptedException;

    /**
     * Method return if this instance is currently activate.
     * @return is true if the instance is active and false if not. 
     */
	public boolean isActive();
}