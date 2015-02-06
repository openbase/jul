/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author divine
 */
public class CouldNotPerformException extends Exception {

	public CouldNotPerformException(String message) {
		super(message);
	}

	public CouldNotPerformException(String message, Throwable cause) {
		super(message, cause);
	}

	public CouldNotPerformException(Throwable cause) {
		super(cause);
	}

    public CouldNotPerformException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
