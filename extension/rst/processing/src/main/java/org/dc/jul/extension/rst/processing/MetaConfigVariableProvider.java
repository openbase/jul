package org.dc.jul.extension.rst.processing;


import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.processing.VariableProvider;
import rst.configuration.MetaConfigType.MetaConfig;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class MetaConfigVariableProvider implements VariableProvider {

    private final String name;
    private final MetaConfig metaConfig;

    public MetaConfigVariableProvider(final String name, final MetaConfig metaConfig) {
        this.name = name;
        this.metaConfig = metaConfig;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getValue(String variable) throws NotAvailableException {
        return MetaConfigProcessor.getValue(metaConfig, variable);
    }
}
