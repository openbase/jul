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
     * @param identifiere
     */
    public NotAvailableException(final String identifiere) {
        super("Context[" + identifiere + "] is not available!");
    }

    /**
     *
     * @param identifiere
     * @param cause
     */
    public NotAvailableException(final String identifiere, final Throwable cause) {
        super("Context[" + identifiere + "] is not available!", cause);
    }

    /**
     *
     * @param clazz
     * @param identifiere
     */
    public NotAvailableException(final Class clazz, final String identifiere) {
        this(clazz.getSimpleName(), identifiere);
    }

    /**
     *
     * @param context
     * @param identifiere
     */
    public NotAvailableException(final String context, final String identifiere) {
        super(context + "[" + identifiere + "]] is not available!");
    }

    /**
     *
     * @param context
     * @param identifiere
     * @param cause
     */
    public NotAvailableException(final String context, final String identifiere, final Throwable cause) {
        super(context + "[" + identifiere + "]] is not available!", cause);
    }

    public NotAvailableException(final String context, final Object identifiere, final Throwable cause) {
        this(context, identifiere.toString(), cause);
    }

    public NotAvailableException(final Class context, final Object identifiere, final Throwable cause) {
        this(context, identifiere.toString(), cause);
    }

    public NotAvailableException(final String context, final String identifiere, final String message) {
        super(context + "[" + identifiere + "]] is not available! " + message);
    }

    /**
     *
     * @param context
     * @param identifiere
     * @param cause
     */
    public NotAvailableException(final Class context, final String identifiere, final Throwable cause) {
        this(context.getSimpleName(), identifiere, cause);
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
