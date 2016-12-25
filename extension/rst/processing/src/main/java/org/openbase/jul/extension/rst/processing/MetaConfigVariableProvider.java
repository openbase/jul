package org.openbase.jul.extension.rst.processing;

/*
 * #%L
 * JUL Extension RST Processing
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.processing.VariableProvider;
import rst.configuration.MetaConfigType.MetaConfig;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class MetaConfigVariableProvider implements VariableProvider {

    private final String name;
    private final MetaConfig metaConfig;

    public MetaConfigVariableProvider(final String name, final MetaConfig metaConfig) {
        this.name = name;
        this.metaConfig = metaConfig;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String getName() {
        return name;
    }

    /**
     *
     * @param variable {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public String getValue(String variable) throws NotAvailableException {
        return MetaConfigProcessor.getValue(metaConfig, variable);
    }

    /**
     *
     * @param variableContains {@inheritDoc}
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public Map<String, String> getValues(String variableContains) throws NotAvailableException {
        return MetaConfigProcessor.getValues(metaConfig, variableContains);
    }

}
