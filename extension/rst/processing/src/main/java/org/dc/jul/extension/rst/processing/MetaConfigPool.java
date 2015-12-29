/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rst.processing;

import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.processing.VariableProcessor;
import org.dc.jul.processing.VariableProvider;
import java.util.ArrayList;
import java.util.Collection;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
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
