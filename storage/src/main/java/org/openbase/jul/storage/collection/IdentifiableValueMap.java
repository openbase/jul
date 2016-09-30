package org.openbase.jul.storage.collection;

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

import org.openbase.jul.iface.Identifiable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class IdentifiableValueMap<ID extends Identifiable> extends HashMap<ID, Identifiable<ID>> {

    public IdentifiableValueMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IdentifiableValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentifiableValueMap() {
    }

    public IdentifiableValueMap(Map<? extends ID, ? extends Identifiable<ID>> m) {
        super(m);
    }
    
    
    
    
}
