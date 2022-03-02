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
 * Simple holder interface which holds an throwable instance and provides getter, setter, checks and throw methods to handle the internal field.
 * Holder can be used to pass an throwable instance from inside of a lamda expression to the outer method scope.
 * @param <T>
 */
public interface ThrowableValueHolder<T extends Throwable> extends ValueHolder<T> {

    /**
     * Method thrown the internal throwable if available.
     * @throws T the thrown throwable.
     */
    void throwIfAvailable() throws T;
}
