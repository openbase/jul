package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing Default
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import java.util.Map;
import org.openbase.jul.exception.NotAvailableException;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface VariableProvider {

    String VARIABLE_INITIATOR = "${";
    String VARIABLE_TERMINATOR = "}";

    /**
     * Method return the name of this provider.
     * The name should inform about the origin.
     *
     * @return the name as string.
     */
    String getName();

    /**
     * 
     * @param variable the variable name to be resolved.
     * @return the value of the variable.
     * @throws NotAvailableException is thrown in case the variable could not be resolved.
     */
    String getValue(final String variable) throws NotAvailableException;

    /**
     *
     * @param variable the variable name to be resolved.
     * @param defaultValue the value to return in case the variable could not be resolved.
     *                 
     * @return the value of the variable.
     */
    default String getValue(final String variable, final String defaultValue) {
        try {
            return getValue(variable);
        } catch (NotAvailableException e) {
            return defaultValue;
        }
    }

    /**
     * Method resolves all variables whose name contains the given identifier.
     *
     * @param variableContains the identifier to select the variables.
     * @return a map of the variable name and its current value.
     */
    Map<String, String> getValues(final String variableContains);
    
}
