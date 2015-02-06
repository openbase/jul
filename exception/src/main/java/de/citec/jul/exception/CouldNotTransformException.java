/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author mpohling
 */
public class CouldNotTransformException extends CouldNotPerformException {

    public CouldNotTransformException(String message) {
        super(message);
    }

    public CouldNotTransformException(String message, Throwable cause) {
        super(message, cause);
    }

    public CouldNotTransformException(Throwable cause) {
        super(cause);
    }
    
}
