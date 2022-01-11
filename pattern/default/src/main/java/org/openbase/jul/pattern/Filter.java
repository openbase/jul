package org.openbase.jul.pattern;

/*-
 * #%L
 * JUL Pattern Default
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

/**
 * Filter which decides for a list of objects which to keep and which to filter out.
 *
 * @param <T> the type of object on which the filter works
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.de">Tamino Huxohl</a>
 */
public interface Filter<T> {

    /**
     * Check if the type passes the filter.
     *
     * @param type the object which is checked.
     * @return true if the item passes the filter.
     */
    default boolean pass(T type) {
        return !match(type);
    }

    /**
     * Check if the type matches the filter.
     *
     * @param type the object which is checked.
     * @return true if this item is filtered.
     */
    boolean match(T type);
}
