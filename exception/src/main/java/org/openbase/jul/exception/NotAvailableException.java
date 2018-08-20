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
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class NotAvailableException extends InvalidStateException {

    /**
     * Method generates a suitable exception message out of the given {@code identifier} and {@code message}.
     *
     * @param identifier a class used as identifier of the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final Class identifier, final Throwable cause) {
        this(ContextType.CLASS, identifier.getSimpleName(), cause);
    }

    /**
     * Method generates a suitable exception message with the given {@code identifier}.
     *
     * @param identifier a class used as identifier of the missing context.
     */
    public NotAvailableException(final Class identifier) {
        this(ContextType.CLASS, identifier.getSimpleName());
    }

    /**
     * Method generates a suitable exception message with the given {@code identifier}.
     *
     * @param identifier an instance providing a toString() method to identify the missing context.
     */
    public NotAvailableException(final Object identifier) {
        this(identifier.toString());
    }

    /**
     * Method generates a suitable exception message out of the given {@code context} and {@code identifier}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     */
    public NotAvailableException(final String context, final Object identifier) {
        this(context, identifier.toString());
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final String context, final Object identifier, final Throwable cause) {
        this(context, identifier.toString(), cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context} and {@code identifier}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     */
    public NotAvailableException(final ContextType context, final Object identifier) {
        this(context, identifier.toString());
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final ContextType context, final Object identifier, final Throwable cause) {
        this(context, identifier.toString(), cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context} and {@code identifier}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     */
    public NotAvailableException(final Class context, final Object identifier) {
        this(context.getSimpleName(), identifier.toString());
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a class to describe the missing context.
     * @param identifier an identifier to describe the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final Class context, final Object identifier, final Throwable cause) {
        this(context.getSimpleName(), identifier.toString(), cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code identifier} and {@code message}.
     *
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final Object identifier, final String message) {
        this((String) null, identifier.toString(), message);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final String context, final Object identifier, final String message) {
        this(context, identifier.toString(), message);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final String context, final Object identifier, final String message, final Throwable cause) {
        this(context, identifier.toString(), message, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final ContextType context, final Object identifier, final String message) {
        this(context, identifier.toString(), message);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final ContextType context, final Object identifier, final String message, final Throwable cause) {
        this(context, identifier.toString(), message, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a class to describe the missing context.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final Class context, final Object identifier, final String message) {
        this(context.getSimpleName(), identifier.toString(), message);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a class to describe the missing context.
     * @param identifier an instance providing a toString() method to identify the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final Class context, final Object identifier, final String message, final Throwable cause) {
        this(context.getSimpleName(), identifier.toString(), message, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context} and {@code identifier}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     */
    public NotAvailableException(final Class context, final String identifier) {
        this(context.getSimpleName(), identifier);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a class to describe the missing context.
     * @param identifier an identifier to describe the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final Class context, final String identifier, final Throwable cause) {
        this(context.getSimpleName(), identifier, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a class to describe the missing context.
     * @param identifier an identifier to describe the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final Class context, final String identifier, final String message) {
        this(context.getSimpleName(), identifier, message);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a class to describe the missing context.
     * @param identifier an identifier to describe the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final Class context, final String identifier, final String message, final Throwable cause) {
        this(context.getSimpleName(), identifier, message, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context} and {@code identifier}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     */
    public NotAvailableException(final ContextType context, final String identifier) {
        this((context == null || context == ContextType.USE_ID_AS_CONTEXT ? null : context.getName()), identifier);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final ContextType context, final String identifier, final Throwable cause) {
        this((context == null || context == ContextType.USE_ID_AS_CONTEXT ? null : context.getName()), identifier, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final ContextType context, final String identifier, final String message) {
        this((context == null || context == ContextType.USE_ID_AS_CONTEXT ? null : context.getName()), identifier, message);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a enum which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final ContextType context, final String identifier, final String message, final Throwable cause) {
        this((context == null || context == ContextType.USE_ID_AS_CONTEXT ? null : context.getName()), identifier, message, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context} and {@code identifier}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     */
    public NotAvailableException(final String context, final String identifier) {
        this(context, identifier, (String) null);
    }

    /**
     * Method generates a suitable exception message with the given {@code identifier}.
     *
     * @param identifier an identifier to describe the missing context.
     */
    public NotAvailableException(final String identifier) {
        this((String) null, identifier, (String) null);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final String context, final String identifier, final Throwable cause) {
        this(context, identifier, null, cause);
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     */
    public NotAvailableException(final String context, final String identifier, final String message) {
        super((context == null ? identifier : context + "[" + identifier + "]") + " is not available!" + (message == null ? "" : " " + message));
    }

    /**
     * Method generates a suitable exception message out of the given {@code context}, {@code identifier} and {@code message}.
     *
     * @param context    a keyword which describes the type of what is missing.
     * @param identifier an identifier to describe the missing context.
     * @param message    an additional message which is added to the end of the generated message.
     * @param cause      the cause of this exception.
     */
    public NotAvailableException(final String context, final String identifier, final String message, final Throwable cause) {
        super((context == null ? identifier : context + "[" + identifier + "]") + " is not available!" + (message == null ? "" : " " + message), cause);
    }

    /**
     * {@inheritDoc}
     *
     * @param cause {@inheritDoc}
     */
    public NotAvailableException(final Throwable cause) {
        super(cause);
    }

    /**
     * {@inheritDoc}
     *
     * @param message {@inheritDoc}
     * @param cause   {@inheritDoc}
     */
    public NotAvailableException(final String message, final Throwable cause) {
        super(message, cause);
    }

    /**
     * {@inheritDoc}
     *
     * @param message            {@inheritDoc}
     * @param cause              {@inheritDoc}
     * @param enableSuppression  {@inheritDoc}
     * @param writableStackTrace {@inheritDoc}
     */
    public NotAvailableException(final String message, final Throwable cause, final boolean enableSuppression, final boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public enum ContextType {
        USE_ID_AS_CONTEXT,
        IDENTIFIER,
        INSTANCE,
        DATA,
        CLASS,
        STATE,
        CONNECTION;

        public String getName() {
            return name().charAt(0) + name().substring(1).toLowerCase();
        }
    }
}
