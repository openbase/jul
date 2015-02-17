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
public class InvocationFailedException extends CouldNotPerformException {

    public InvocationFailedException(Object callable, Object target, final Throwable cause) {
        this(callable.getClass(), target.getClass(), cause);
    }
    public InvocationFailedException(Class callable, Class target, final Throwable cause) {
        super("Coudl not invoke "+callable.getSimpleName()+" on "+target.getName()+".", cause);
    }    
}
