package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing
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
import java.util.TreeMap;
import org.openbase.jul.exception.NotAvailableException;

/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class VariableStore implements VariableProvider {

    private final String name;
    private final Map<String, String> variableMap;

    public VariableStore(final String name) {
        this.name = name;
        this.variableMap = new TreeMap<>();
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * Resolves the value for the given key.
     * @param key
     * @return
     * @throws NotAvailableException
     */
    @Override
    public String getValue(String key) throws NotAvailableException {
        return variableMap.get(key);
    }

    /**
     * Stores the key value pair into the variable Store.
     * @param key
     * @param value
     */
    public void store(final String key, final String value) {
        variableMap.put(key, value);
    }
}
