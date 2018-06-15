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

/**
 * Simple holder class which holds an value instance and provides getter, setter and checks to handle the internal value field.
 * Holder can be used to pass an value instance from inside of a lamda expression to the outer method scope.
 *
 * @param <V> the type of the internal value.
 */
public class ValueHolderImpl<V> implements ValueHolder<V> {

    /**
     * The internal value field.
     */
    protected V value;

    /**
     * Constructor creates a new value holder instance.
     *
     * @param value the initial value to setup.
     */
    public ValueHolderImpl(V value) {
        this.value = value;
    }

    /**
     * Method creates a new generic value holder.
     */
    public ValueHolderImpl() {
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public V getValue() {
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @param exception {@inheritDoc}
     */
    @Override
    public void setValue(V exception) {
        this.value = exception;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isValueAvailable() {
        return value != null;
    }
}
