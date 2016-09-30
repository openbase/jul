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
import org.openbase.jul.iface.Shutdownable;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * ConsistencyHandler can be registered at any registry type and will be informed about data changes via the processData Method.
 * The handler can be used to establish a registry data consistency.
 * @param <KEY> the registry key type.
 * @param <VALUE> the registry data value type.
 * @param <MAP>
 * @param <R>
 */
public interface ConsistencyHandler<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends Registry<KEY, VALUE>> extends Shutdownable {

    /**
     * Method for establishing a registry data consistency.
     * Method is called by the registry in case of entry changes.
     *
     * @param id
     * @param entry
     * @param entryMap the entry map of the underlying registry.
     * @param registry the underlying registry.
     * @throws CouldNotPerformException thrown to handle errors.
     * @throws org.openbase.jul.storage.registry.EntryModification in case of entry modification during consistency process.
     */
    public void processData(final KEY id, final VALUE entry, final MAP entryMap, final R registry) throws CouldNotPerformException, EntryModification;

    /**
     * Method can be internally used to clear any temporally maps or other caches which are only used for one consistency check iteration.
     * This method is called each time the registry consistency check starts a new iteration.
     *
     * In some cases it makes sense to cache any properties e.g. scopes or ids which should be globally unique. This check can be performed by caching these values within a map.
     * To make sure these maps do not interfere between different consistency check iterations this method can be used to clear these kind of caches.
     *
     */
    public void reset();
}
