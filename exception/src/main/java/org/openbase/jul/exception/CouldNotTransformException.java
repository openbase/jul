package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
public class CouldNotTransformException extends CouldNotPerformException {

    public CouldNotTransformException(final String message) {
        super(message);
    }

    public CouldNotTransformException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public CouldNotTransformException(final Throwable cause) {
        super(cause);
    }

    public CouldNotTransformException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public CouldNotTransformException(final Object source, final Class destination, final String message) {
        this(source, destination.getClass().getName(), message);
    }

    public CouldNotTransformException(final Object source, final Class destination, final String message, final Throwable cause) {
        this(source, destination.getClass().getName(), message, cause);
    }

    public CouldNotTransformException(final Object source, final Class destination, final Throwable cause) {
        this(source, destination.getClass().getName(), cause);
    }

    public CouldNotTransformException(final Object source, final Object destination, final String message) {
        super("Could not transform " + source + " into " + destination + ": " + message);
    }

    public CouldNotTransformException(final Object source, final Object destination, final String message, final Throwable cause) {
        super("Could not transform " + source + " into " + destination + ": " + message, cause);
    }

    public CouldNotTransformException(final Object source, final Object destination, final Throwable cause) {
        super("Could not transform " + source + " into " + destination + "!", cause);
    }

}
