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
public class NotSupportedException extends CouldNotPerformException {

	public NotSupportedException(String message, Object context, Object source) {
		super("Given "+context+" is not supported by "+source+": "+message);
	}

	public NotSupportedException(Object context, Object source) {
		super("Given "+context+" is not supported by "+source+"!");
	}

	public NotSupportedException(Object context, Object source, Throwable cause) {
		super("Given "+context+" is not supported by "+source+"!", cause);
	}

	public NotSupportedException(String message,Object context, Object source, Throwable cause) {
		super("Given "+context+" is not supported by "+source+": "+message, cause);
	}
}
