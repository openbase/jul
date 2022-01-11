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

import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 * Filter which decides for a list of objects which to keep and which to filter out.
 * 
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.de">Tamino Huxohl</a>
 * @param <T> the type of object on which the filter works
 */
public abstract class AbstractFilter<T> implements ListFilter<T> {

    /**
     * Filter object from the list for which the verification fails.
     * 
     * @param list the list which is filtered
     * @return a filtered list
     * @throws CouldNotPerformException if an error occurs while filtering
     *
     */
    @Override
    public List<T> filter(final List<T> list) throws CouldNotPerformException {
        beforeFilter();
        return ListFilter.super.filter(list);
    }

    /**
     * This method is called once before the filtering is applied.
     * 
     * @throws CouldNotPerformException if an error occurs.
     */
    public abstract void beforeFilter() throws CouldNotPerformException;

    
    /**
     * A filter can depend on some other processes. To be notified
     * when the filter will change an observer can be registered.
     * 
     * @param observer An observer which is notified when the filter changes.
     */
    public abstract void addObserver(Observer observer);
    
    /**
     * Remove an observer which is added by addObserver.
     * 
     * @param observer The observer to be removed.
     */
    public abstract void removeObserver(Observer observer);

}
