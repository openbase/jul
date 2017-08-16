package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.HashMap;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.iface.Identifiable;

/**
 * A simple {@code AbstractRegistry} implementation providing the timestamp of the last synchronization by implementing the {@code SynchronizableRegistry} interface.
 *
 * @author <a href="mailto:thuppke@techfak.uni-bielefeld.de">Thoren Huppke</a>
 * @param <KEY> The registry key type.
 * @param <ENTRY> The registry entry type.
 */
public class SynchronizableRegistryImpl<KEY, ENTRY extends Identifiable<KEY>> extends RegistryImpl<KEY, ENTRY> implements SynchronizableRegistry<KEY, ENTRY> {

    /**
     * Never synchronized constant.
     */
    public static final long NEVER_SYNCHRONIZED = -1;

    /**
     * This variable provides the latest synchronization timestamp.
     */
    private long lastSynchronizationTimestamp = NEVER_SYNCHRONIZED;

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
     * {@inheritdoc}
     */
    @Override
    public void notifySynchronization() {
        lastSynchronizationTimestamp = System.currentTimeMillis();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isInitiallySynchronized() {
        return lastSynchronizationTimestamp != NEVER_SYNCHRONIZED;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public long getLastSynchronizationTimestamp() throws NotAvailableException {
        if (!isInitiallySynchronized()) {
            throw new NotAvailableException("SynchronizationTimestamp", new InvalidStateException("ControllerRegistry was never fully synchronized yet!"));
        }
        return lastSynchronizationTimestamp;
    }
}
