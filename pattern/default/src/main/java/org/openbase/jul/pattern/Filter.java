package org.openbase.jul.pattern;

/*-
 * #%L
 * JUL Pattern Default
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

import org.openbase.jul.annotation.Experimental;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * Filter which decides for a list of objects which to keep and which to filter out.
 *
 * @param <T> the type of object on which the filter works
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.de">Tamino Huxohl</a>
 */
public interface Filter<T> {

    // todo release: rename method "filter" into "match" and provide "pass" method?
    // todo release: true if it should be kept and else false??? does it really make sense? "Do not filter if filter returns true?"
    // todo release: is the experimental code maybe a solution?

    /**
     * Verifies an object of type t.
     *
     * @param type the object which is verified
     * @return true if it should be kept and else false
     * @throws CouldNotPerformException if the verification fails
     */
    //@Deprecated
    boolean filter(T type) throws CouldNotPerformException;

    @Experimental
    default boolean pass(T type) throws CouldNotPerformException {
        return !match(type);
    }

    @Experimental
    default boolean match(T type) throws CouldNotPerformException{
        // This is just an workaround. Should be declared via implementation after filter method has been marked as deprecated.
        return !filter(type);
    }
}
