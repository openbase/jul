/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.exception;

/**
 *
 * @author Divine Threepwood
 */
public class NotInitializedException extends InvalidStateException {

	public NotInitializedException(String message, Object context) {
		super("Given "+context+" is not initialized yet: "+message);
	}

	public NotInitializedException(Object context) {
		super("Given "+context+" is not initialized yet!");
	}

	public NotInitializedException(Object context, Throwable cause) {
		super("\"Given \"+context+\" is not initialized yet!", cause);
	}

	public NotInitializedException(String message,Object context, Throwable cause) {
		super("Given "+context+" is not initialized yet: "+message, cause);
	}
}
