package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.storage.registry.plugin.RegistryPlugin;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public class RegistryImpl<KEY, ENTRY extends Identifiable<KEY>> extends AbstractRegistry<KEY, ENTRY, HashMap<KEY, ENTRY>, RegistryImpl<KEY, ENTRY>, RegistryPlugin<KEY, ENTRY, RegistryImpl<KEY, ENTRY>>> {

    public RegistryImpl(HashMap<KEY, ENTRY> entryMap) throws InstantiationException {
        super(entryMap);
    }

    public RegistryImpl() throws InstantiationException {
        super(new HashMap<>());
    }

    /**
     * Just print on debug level.
     * @param message 
     */
    public void log(final String message) {
        logger.debug(message);
    }
}
