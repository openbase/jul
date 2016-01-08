/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.exception;

/**
 *
 * @author divine
 */
public class InstantiationException extends CouldNotPerformException {


	public InstantiationException(final Class clazz, final Throwable cause) {
		super("Could not instantiate "+clazz.getSimpleName()+"!", cause);
	}

	public InstantiationException(final Class clazz, final String identifiere, final Throwable cause) {
		super("Could not instantiate "+clazz.getSimpleName()+"["+identifiere+"]!", cause);
	}

    public InstantiationException(final Object instance, final String identifiere, final Throwable cause) {
        this(instance.getClass(), identifiere, cause);
    }

	public InstantiationException(final Object instance, final Throwable cause) {
		this(instance.getClass(), cause);
	}
}
