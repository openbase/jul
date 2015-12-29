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
