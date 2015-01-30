/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

/**
 *
 * @author divine
 */
public class ConstructionException extends Exception {

	public ConstructionException(String message, Class clazz) {
		super("Coult not create "+clazz.getSimpleName()+" instance: " +message);
	}

	public ConstructionException(String message, Class clazz, Throwable cause) {
		super("Coult not create "+clazz.getSimpleName()+" instance: " +message, cause);
	}

	public ConstructionException(Class clazz, Throwable cause) {
		super("Coult not create "+clazz.getSimpleName()+" instance!", cause);
	}

//	public ConstructionException(String message, Object instance) {
//		this(message, instance.getClass());
//	}

	public ConstructionException(String message, Object instance, Throwable cause) {
		this(message, instance.getClass(), cause);
	}

	public ConstructionException(Object instance, Throwable cause) {
		this(instance.getClass(), cause);
	}
}
