/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.exception;

/**
 *
 * @author mpohling
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
