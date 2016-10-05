package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
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
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Identifiable;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public interface RegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends Registry<KEY, ENTRY>> extends Registry<KEY, ENTRY> {

    public void sync(final MAP map) throws CouldNotPerformException;

    public void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException;

    public void removeConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException;

    void replaceInternalMap(Map<KEY, ENTRY> map) throws CouldNotPerformException;

    public ENTRY load(final ENTRY entry) throws CouldNotPerformException;

}
