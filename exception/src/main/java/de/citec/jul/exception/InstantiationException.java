/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author divine
 */
public class InstantiationException extends CouldNotPerformException {

	public InstantiationException(String message, Class clazz) {
		super("Coult not create "+clazz.getSimpleName()+" instance: " +message);
	}

	public InstantiationException(String message, Class clazz, Throwable cause) {
		super("Coult not create "+clazz.getSimpleName()+" instance: " +message, cause);
	}

	public InstantiationException(Class clazz, Throwable cause) {
		super("Coult not create "+clazz.getSimpleName()+" instance!", cause);
	}

	public InstantiationException(String message, Object instance, Throwable cause) {
		this(message, instance.getClass(), cause);
	}

	public InstantiationException(Object instance, Throwable cause) {
		this(instance.getClass(), cause);
	}
}
