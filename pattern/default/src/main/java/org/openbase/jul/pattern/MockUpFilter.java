package org.openbase.jul.pattern;

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

/**
 * Default filter which does not pass at all.
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.de">Tamino Huxohl</a>
 * @param <T> The type of value which is filtered.
 */
public class MockUpFilter<T> extends AbstractFilter<T>{

    /**
     * Has nothing to do before filtering.
     */
    @Override
    public void beforeFilter() {
        // do nothing
    }

    /**
     * Matches no values.
     * 
     * @param type The message which is checked.
     * @return True for all messages.
     */
    @Override
    public boolean match(T type) {
        // every value is fine
        return false;
    }

    /**
     * This filter does nothing so no observer will be added.
     * {@inheritDoc}
     * 
     * @param observer {@inheritDoc}
     */
    @Override
    public void addObserver(Observer observer) {
    }

    /**
     * No observer can be added so removal will also do nothing.
     * {@inheritDoc}
     * 
     * @param observer {@inheritDoc}
     */
    @Override
    public void removeObserver(Observer observer) {
    }
}
