package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Identifiable;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public interface SynchronizableRegistry<KEY, ENTRY extends Identifiable<KEY>> extends Registry<KEY, ENTRY> {

    /**
     * Informs the controller registry that the synchronization with the external controller configuration registry was done.
     */
    public void notifySynchronization();

    /**
     * Method returns the initial synchronization state.
     *
     * @return true if the the initial synchronization is done.
     */
    public boolean isInitiallySynchronized();

    /**
     * Method returns the timestamp of the last synchronization.
     *
     * @return the timestamp in milliseconds.
     * @throws NotAvailableException is thrown in case the synchronization was never performed.
     */
    public long getLastSynchronizationTimestamp() throws NotAvailableException;

}
