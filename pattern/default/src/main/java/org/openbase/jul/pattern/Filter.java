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

import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * Filter which decides for a list of objects which to keep and which to filter out.
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.de">Tamino Huxohl</a>
 * @param <T> the type of object on which the filter works
 */
public interface Filter<T> {

    /**
     * Filter object from the list for which the verification fails.
     *
     * @param list the list which is filtered
     * @return a filtered list
     * @throws CouldNotPerformException if an error occurs while filtering
     */
    List<T> filter(List<T> list) throws CouldNotPerformException;

    /**
     * Verifies an object of type t.
     *
     * @param type the object which is verified
     * @return true if it should be kept and else false
     * @throws CouldNotPerformException if the verification fails
     */
    boolean verify(T type) throws CouldNotPerformException;
    
}
