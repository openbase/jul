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

    public NotAvailableException(Object object, String message) {
        this(object.getClass(), message);
    }

    public NotAvailableException(Object object, Throwable cause) {
        this(object.getClass(), cause);
    }

    public NotAvailableException(Class clazz, String message) {
        this(clazz.getSimpleName(), message);
    }

    public NotAvailableException(Class clazz, Throwable cause) {
        this(clazz.getSimpleName(), cause);
    }

    public NotAvailableException(Object object) {
        this(object.getClass().getSimpleName());
    }

    public NotAvailableException(Class clazz) {
        this(clazz.getSimpleName());
    }

    public NotAvailableException(String objectName, String message, Throwable cause) {
        super("Context[" + objectName + "] is not available! " + message, cause);
    }

    public NotAvailableException(String objectName, String message) {
        super("Context[" + objectName + "] is not available! " + message);
    }

    public NotAvailableException(String objectName) {
        super("Context[" + objectName + "] is not available!");
    }

    public NotAvailableException(String objectName, Throwable cause) {
        super("Context[" + objectName + "] is not available!", cause);
    }

    public NotAvailableException(Throwable cause) {
        super(cause);
    }

    public NotAvailableException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
