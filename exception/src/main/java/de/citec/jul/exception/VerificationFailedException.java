/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author divine
 */
public class VerificationFailedException extends Exception {

	public VerificationFailedException(Throwable cause) {
		super(cause);
	}

	public VerificationFailedException(String message, Throwable cause) {
		super(message, cause);
	}

	public VerificationFailedException(String message) {
		super(message);
	}

	public VerificationFailedException() {
	}
	
}
