package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;

import java.util.HashMap;

/**
 * A simple {@code AbstractRegistry} implementation providing the timestamp of the last synchronization by implementing the {@code SynchronizableRegistry} interface.
 *
 * @param <KEY>   The registry key type.
 * @param <ENTRY> The registry entry type.
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 */
public class SynchronizableRegistryImpl<KEY, ENTRY extends Identifiable<KEY>> extends RegistryImpl<KEY, ENTRY> implements SynchronizableRegistry<KEY, ENTRY> {

    /**
     * Observable notifying when a synchronization is complete.
     */
    private final ObservableImpl<Registry<KEY, ENTRY>, Long> synchronisationObservable = new ObservableImpl<>(this);

    /**
     * Creates a new SynchronizableRegistry with a default {@code HashMap} as internal map.
     *
     * @throws InstantiationException is thrown if registry could not be instantiated.
     */
    public SynchronizableRegistryImpl() throws InstantiationException {
        super(new HashMap<>());
    }

    /**
     * Creates a new SynchronizableRegistry where the given {@code entryMap} is used as internal map.
     *
     * @param entryMap a map instance to be used as internal map.
     * @throws InstantiationException is thrown if registry could not be instantiated.
     */
    public SynchronizableRegistryImpl(final HashMap<KEY, ENTRY> entryMap) throws InstantiationException {
        super(entryMap);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void notifySynchronization() {
        try {
            synchronisationObservable.notifyObservers(System.currentTimeMillis());
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify observer about synchronization", ex), logger);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isInitiallySynchronized() {
        return synchronisationObservable.isValueAvailable();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public long getLastSynchronizationTimestamp() throws NotAvailableException {
        return synchronisationObservable.getValue();
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void addSynchronizationObserver(Observer<Registry<KEY, ENTRY>,Long> observer) {
        synchronisationObservable.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void removeSynchronizationObserver(Observer<Registry<KEY, ENTRY>, Long> observer) {
        synchronisationObservable.removeObserver(observer);
    }
}
