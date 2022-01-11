package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
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
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class InstantiationException extends CouldNotPerformException {

    /**
     * {@inheritDoc}
     *
     * @param message {@inheritDoc}
     * @param cause   {@inheritDoc}
     */
    public InstantiationException(String message, Throwable cause) {
        super(message, cause);
    }

    public InstantiationException(final Class clazz, final Throwable cause) {
        super("Could not instantiate " + clazz.getSimpleName() + "!", cause);
    }

    public InstantiationException(final Class clazz, final String identifiere, final Throwable cause) {
        super("Could not instantiate " + clazz.getSimpleName() + "[" + identifiere + "]!", cause);
    }

    public InstantiationException(final Object instance, final String identifiere, final Throwable cause) {
        this(instance instanceof Class ? (Class) instance : instance.getClass(), identifiere, cause);
    }

    public InstantiationException(final Object instance, final Throwable cause) {
        this(instance instanceof Class ? (Class) instance : instance.getClass(), cause);
    }
}
