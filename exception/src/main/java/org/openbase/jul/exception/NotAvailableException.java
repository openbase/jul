package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
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
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class NotAvailableException extends InvalidStateException {

    /**
     *
     * @param object
     * @param cause
     */
    public NotAvailableException(Object object, Throwable cause) {
        this(object.getClass(), cause);
    }

    /**
     *
     * @param clazz
     * @param cause
     */
    public NotAvailableException(Class clazz, Throwable cause) {
        this(clazz.getSimpleName(), cause);
    }

    /**
     *
     * @param object
     */
    public NotAvailableException(Object object) {
        this(object.getClass().getSimpleName());
    }

    /**
     *
     * @param clazz
     */
    public NotAvailableException(Class clazz) {
        this(clazz.getSimpleName());
    }

    /**
     *
     * @param identifier
     */
    public NotAvailableException(final String identifier) {
        super("Context[" + identifier + "] is not available!");
    }

    /**
     *
     * @param identifier
     * @param cause
     */
    public NotAvailableException(final String identifier, final Throwable cause) {
        super("Context[" + identifier + "] is not available!", cause);
    }

    /**
     *
     * @param clazz
     * @param identifier
     */
    public NotAvailableException(final Class clazz, final String identifier) {
        this(clazz.getSimpleName(), identifier);
    }

    /**
     *
     * @param context
     * @param identifier
     */
    public NotAvailableException(final String context, final String identifier) {
        super(context + "[" + identifier + "]] is not available!");
    }

    /**
     *
     * @param context
     * @param identifier
     * @param cause
     */
    public NotAvailableException(final String context, final String identifier, final Throwable cause) {
        super(context + "[" + identifier + "]] is not available!", cause);
    }

    public NotAvailableException(final String context, final Object identifier, final Throwable cause) {
        this(context, identifier.toString(), cause);
    }

    public NotAvailableException(final Class context, final Object identifier, final Throwable cause) {
        this(context, identifier.toString(), cause);
    }

    public NotAvailableException(final String context, final String identifier, final String message) {
        super(context + "[" + identifier + "]] is not available! " + message);
    }

    /**
     *
     * @param context
     * @param identifier
     * @param cause
     */
    public NotAvailableException(final Class context, final String identifier, final Throwable cause) {
        this(context.getSimpleName(), identifier, cause);
    }

    /**
     *
     * @param cause
     */
    public NotAvailableException(Throwable cause) {
        super(cause);
    }

    /**
     *
     * @param message
     * @param cause
     * @param enableSuppression
     * @param writableStackTrace
     */
    public NotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
