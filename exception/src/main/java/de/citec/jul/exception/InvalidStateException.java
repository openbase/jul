/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public class InvalidStateException extends Exception {

	public InvalidStateException(String message) {
		super(message);
	}

	public InvalidStateException(String message, Throwable cause) {
		super(message, cause);
	}

	public InvalidStateException(Throwable cause) {
		super(cause);
	}

	public InvalidStateException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}
	
}
