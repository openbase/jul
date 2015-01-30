/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author divine
 */
public class VerificationFaildException extends Exception {

	public VerificationFaildException(Throwable cause) {
		super(cause);
	}

	public VerificationFaildException(String message, Throwable cause) {
		super(message, cause);
	}

	public VerificationFaildException(String message) {
		super(message);
	}

	public VerificationFaildException() {
	}
	
}
