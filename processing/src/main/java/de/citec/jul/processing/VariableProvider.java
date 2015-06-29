/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

import de.citec.jul.exception.NotAvailableException;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface VariableProvider {

    public final static String VARIABLE_INITIATOR = "${";
    public final static String VARIABLE_TERMINATOR = "}";

    public String getName();
    public String getValue(final String variable) throws NotAvailableException;

}
