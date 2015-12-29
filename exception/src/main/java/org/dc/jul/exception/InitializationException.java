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
public class InitializationException extends CouldNotPerformException {

    public InitializationException(Object context, Throwable cause) {
        this(context.getClass(), cause);
    }
    public InitializationException(Class context, Throwable cause) {
        super("Could not initialize "+context+"!", cause);
    }
}
