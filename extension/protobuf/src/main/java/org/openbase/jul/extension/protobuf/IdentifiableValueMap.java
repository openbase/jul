package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @param <KEY>
 * @param <VALUE>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class IdentifiableValueMap<KEY, VALUE extends Identifiable<KEY>> extends HashMap<KEY, VALUE> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public IdentifiableValueMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IdentifiableValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentifiableValueMap() {
    }

    public IdentifiableValueMap(Map<? extends KEY, ? extends VALUE> m) {
        super(m);
    }

    public void put(final VALUE value) throws CouldNotPerformException {
        try {
            put(value.getId(), value);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not put value to list!", ex);
        }
    }

    public VALUE removeValue(Identifiable<KEY> value) throws CouldNotPerformException {
        return super.remove(value.getId());
    }

    public static <K, V extends Identifiable<K>> IdentifiableValueMap<K, V> fromCollection(final Collection<V> valueCollection) {
        final IdentifiableValueMap<K, V> identifiableValueMap = new IdentifiableValueMap<>();
        final Logger logger = LoggerFactory.getLogger(IdentifiableValueMap.class);

        for (final V value : valueCollection) {
            try {
                identifiableValueMap.put(value);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not add value[" + value + "] to map!", ex), logger);
            }
        }

        return identifiableValueMap;
    }
}
