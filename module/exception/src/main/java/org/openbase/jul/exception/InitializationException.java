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
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class InitializationException extends CouldNotPerformException {

    /**
     * {@inheritDoc}
     *
     * @param message {@inheritDoc}
     * @param cause   {@inheritDoc}
     */
    public InitializationException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Creates a new InitializationException instance.
     *
     * @param context the instance or type which could not be initialized.
     * @param cause   the reason why the initialization failed.
     */
    public InitializationException(Object context, Throwable cause) {
        this(context.getClass(), cause);
    }

    /**
     * Creates a new InitializationException instance.
     *
     * @param context the class which could not be initialized.
     * @param cause   the reason why the initialization failed.
     */
    public InitializationException(Class context, Throwable cause) {
        super("Could not initialize " + context + "!", cause);
    }
}
