package org.openbase.jul.storage.registry.clone;

/*
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

import com.rits.cloning.Cloner;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Identifiable;
import java.util.Map;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 */
public class RITSCloner<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>> implements RegistryCloner<KEY, ENTRY, MAP>{

    private final Cloner cloner;

    public RITSCloner() {
        this.cloner = new Cloner();
    }

    @Override
    public MAP deepCloneRegistryMap(MAP map) throws CouldNotPerformException {
        return cloner.deepClone(map);
    }

    @Override
    public Map<KEY, ENTRY> deepCloneMap(Map<KEY, ENTRY> map) throws CouldNotPerformException {
        return cloner.deepClone(map);
    }

    @Override
    public ENTRY deepCloneEntry(ENTRY entry) throws CouldNotPerformException {
        return cloner.deepClone(entry);
    }
}
