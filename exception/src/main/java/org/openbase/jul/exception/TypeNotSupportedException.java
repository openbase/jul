package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
public class TypeNotSupportedException extends CouldNotPerformException {

    public TypeNotSupportedException(String message) {
        super(message);
    }

    public TypeNotSupportedException(String message, Throwable cause) {
        super(message, cause);
    }

    public TypeNotSupportedException(Throwable cause) {
        super(cause);
    }

    public TypeNotSupportedException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public TypeNotSupportedException(Object target, Class destination) {
        super("\"Type transformation not supported! Can not convert " + target + " to " + destination + "!");
    }

}
