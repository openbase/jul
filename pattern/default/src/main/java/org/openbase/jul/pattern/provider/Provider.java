package org.openbase.jul.pattern.provider;

/*-
 * #%L
 * JUL Pattern Default
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

import org.openbase.jul.exception.NotAvailableException;

/**
 * Generic interface for providing a data object.
 *
 * @param <D> the datatype of the object.
 */
public interface Provider<D> {

    /**
     * Returns the provide data object.
     *
     * @return the value to provide.
     *
     * @throws NotAvailableException is thrown if the provider does not provide something.
     * @throws InterruptedException  is thrown if the thread has been externally interrupted.
     */
    D get() throws NotAvailableException, InterruptedException;
}
