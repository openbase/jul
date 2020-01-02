package org.openbase.jul.pattern;

/*-
 * #%L
 * JUL Pattern Default
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

/**
 * Simple key - value pair implementation to bundle a key and its related
 * value into one solid data type.
 * @param <KEY> The type of key to use.
 * @param <VALUE> The type of value to use.
 */
public class Pair<KEY, VALUE> {

    private KEY key;
    private VALUE value;

    /**
     * Constructor creates a new key value pair.
     * @param key the key of the pair.
     * @param value the value of the pair.
     */
    public Pair(final KEY key, VALUE value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Method returns the key of this pair.
     * @return the key.
     */
    public KEY getKey() {
        return key;
    }

    /**
     * The value of the pair.
     * @return the value.
     */
    public VALUE getValue() {
        return value;
    }
}
