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

import org.openbase.jul.exception.NotAvailableException;

/**
 * Simple holder interface which holds an value instance and provides getter, setter and checks to handle the internal value field.
 * Holder can be used to filter an value instance from inside of a lamda expression to the outer method scope.
 *
 * @param <V> the type of the internal value.
 */
public interface ValueHolder<V> {

    /**
     * Method returns the value if available.
     *
     * @return the internal value.
     *
     * @throws NotAvailableException is thrown if the value was never set.
     */
    V getValue() throws NotAvailableException;

    /**
     * Method stores the given {@code value} within the holder.
     * @param value the value to store.
     */
    void setValue(V value);

    /**
     * Method return is the value was ever set.
     * @return true if the value is available.
     */
    boolean isValueAvailable();
}
