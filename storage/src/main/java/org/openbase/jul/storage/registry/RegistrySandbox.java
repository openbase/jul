package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import java.util.Map;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Identifiable;

/**
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <REGISTRY>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface RegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, REGISTRY extends Registry<KEY, ENTRY>> extends Registry<KEY, ENTRY> {

    void sync(final MAP map);

    void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler) throws CouldNotPerformException;

    void removeConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, REGISTRY> consistencyHandler) throws CouldNotPerformException;

    void replaceInternalMap(final Map<KEY, ENTRY> map) throws CouldNotPerformException;

    ENTRY load(final ENTRY entry) throws CouldNotPerformException;

}
