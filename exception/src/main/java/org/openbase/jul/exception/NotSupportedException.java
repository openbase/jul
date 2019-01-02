package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
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
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class NotSupportedException extends CouldNotPerformException {

    /**
     * Default constructor.
     * @param message the reason as string.
     * @param cause the cause as throwable.
     */
    public NotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportedException(Object context, Class source) {
        this(context, source.getSimpleName());
    }

    public NotSupportedException(Object context, Object source) {
        super(context + " is not supported by " + source + "!");
    }

    public NotSupportedException(Object context, Object source, Throwable cause) {
        super(context + " is not supported by " + source + "!", cause);
    }

    public NotSupportedException(Object context, Object source, String message, Throwable cause) {
        super(context + " is not supported by " + source + ": " + message, cause);
    }

    public NotSupportedException(Object context, Object source, String message) {
        super(context + " is not supported by " + source + ": " + message);
    }
}
