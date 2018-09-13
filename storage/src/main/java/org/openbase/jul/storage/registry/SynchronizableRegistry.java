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
import org.openbase.jul.pattern.Observer;

/**
 * @param <KEY>
 * @param <ENTRY>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface SynchronizableRegistry<KEY, ENTRY extends Identifiable<KEY>> extends Registry<KEY, ENTRY> {

    /**
     * Informs the controller registry that the synchronization with the external controller configuration registry was done.
     */
    void notifySynchronization();

    /**
     * Method returns the initial synchronization state.
     *
     * @return true if the the initial synchronization is done.
     */
    boolean isInitiallySynchronized();

    /**
     * Method returns the timestamp of the last synchronization.
     *
     * @return the timestamp in milliseconds.
     * @throws NotAvailableException is thrown in case the synchronization was never performed.
     */
    long getLastSynchronizationTimestamp() throws NotAvailableException;

    /**
     * Add an observer that is notified after a synchronization.
     *
     * @param observer the observer that will be added.
     */
    void addSynchronizationObserver(Observer<Registry<KEY, ENTRY>, Long> observer);

    /**
     * Remove an observer that is notified after a synchronization.
     *
     * @param observer the observer that will be removed.
     */
    void removeSynchronizationObserver(Observer<Registry<KEY, ENTRY>, Long> observer);

}
