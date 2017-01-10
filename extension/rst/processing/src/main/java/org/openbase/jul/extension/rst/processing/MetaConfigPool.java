package org.openbase.jul.extension.rst.processing;

/*
 * #%L
 * JUL Extension RST Processing
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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

import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.VariableProcessor;
import org.openbase.jul.processing.VariableProvider;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;
import org.openbase.jul.exception.NotSupportedException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class MetaConfigPool implements VariableProvider {

    private final Collection<VariableProvider> variableProviderPool;

    public MetaConfigPool(final Collection<VariableProvider> variableProviderPool) {
        this.variableProviderPool = new ArrayList<>(variableProviderPool);
    }

    public MetaConfigPool() {
        this.variableProviderPool = new ArrayList<>();
    }

    public void register(final VariableProvider provider) {
        variableProviderPool.add(provider);
    }

    @Override
    public String getValue(final String variable) throws NotAvailableException {
        try {
            return VariableProcessor.resolveVariables(VariableProcessor.resolveVariable(variable, variableProviderPool), true, variableProviderPool);
        } catch (MultiException ex) {
            throw new NotAvailableException("Variable[" + variable + "]", ex);
        }
    }

    @Override
    public Map<String, String> getValues(String variableContains) throws NotAvailableException {
        //TODO: Should be implemented.
        throw new NotAvailableException(new NotSupportedException("Method[getValues]", MetaConfigPool.class));
    }
    
    @Override
    public String getName() {
        String provider = "";
        for (VariableProvider variableProvider : variableProviderPool) {
            if(!provider.isEmpty()) {
                provider += ", ";
            }
            provider += variableProvider.getName();
        }
        return getClass().getSimpleName() + "["+provider+"]";
    }
}
