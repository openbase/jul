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
     * @param objectName
     */
    public NotAvailableException(String objectName) {
        super("Context[" + objectName + "] is not available!");
    }

    /**
     *
     * @param objectName
     * @param cause
     */
    public NotAvailableException(String objectName, Throwable cause) {
        super("Context[" + objectName + "] is not available!", cause);
    }

    /**
     *
     * @param objectName
     * @param identifiere
     */
    public NotAvailableException(final Class objectName, final String identifiere) {
        super(objectName+"["+identifiere+"]] is not available!");
    }

    /**
     *
     * @param objectName
     * @param identifiere
     * @param cause
     */
    public NotAvailableException(final Class objectName, final String identifiere, final Throwable cause) {
        super(objectName+"["+identifiere+"]] is not available!", cause);
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
