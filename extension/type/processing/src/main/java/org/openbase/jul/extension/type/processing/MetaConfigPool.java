package org.openbase.jul.extension.type.processing;

/*
 * #%L
 * JUL Extension Type Processing
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

import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.VariableProcessor;
import org.openbase.jul.processing.VariableProvider;

import java.util.*;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class MetaConfigPool implements VariableProvider {

    private final HashMap<String, VariableProvider> variableProviderPool;

    public MetaConfigPool(final Collection<VariableProvider> variableProviders) {
        this();
        for (VariableProvider variableProvider : variableProviders) {
            this.variableProviderPool.put(variableProvider.getName(), variableProvider);
        }
    }

    public MetaConfigPool() {
        this.variableProviderPool = new HashMap<>();
    }

    public void register(final VariableProvider provider) {
        variableProviderPool.put(provider.getName(), provider);
    }

    /**
     * {@inheritDoc}
     *
     * @param variable {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public String getValue(final String variable) throws NotAvailableException {
        try {
            return VariableProcessor.resolveVariables(VariableProcessor.resolveVariable(variable, variableProviderPool.values()), true, variableProviderPool.values());
        } catch (MultiException ex) {
            throw new NotAvailableException("Variable[" + variable + "]", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param variableContains {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Map<String, String> getValues(final String variableContains) {
        final Map<String, String> valueMap = new HashMap<>();
        for (final VariableProvider variableProvider : variableProviderPool.values()) {
            valueMap.putAll(variableProvider.getValues(variableContains));
        }
        return valueMap;
    }

    @Override
    public String getName() {
        String provider = "";
        for (VariableProvider variableProvider : variableProviderPool.values()) {
            if (!provider.isEmpty()) {
                provider += ", ";
            }
            provider += variableProvider.getName();
        }
        return getClass().getSimpleName() + "[" + provider + "]";
    }

    /**
     * Method clears the internal pool.
     */
    public void clear() {
        variableProviderPool.clear();
    }
}
