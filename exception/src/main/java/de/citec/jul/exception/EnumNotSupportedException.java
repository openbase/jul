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
public class EnumNotSupportedException extends NotSupportedException {

    public EnumNotSupportedException(final Enum context, final Class source) {
        super(context.name(), source);
    }
    
    public EnumNotSupportedException(final Enum context, final Object source) {
        this(context, source.getClass());
    }
}
