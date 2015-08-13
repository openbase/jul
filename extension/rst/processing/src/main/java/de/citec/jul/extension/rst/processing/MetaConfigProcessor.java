/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rst.processing;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import rst.configuration.EntryType;
import rst.configuration.MetaConfigType.MetaConfig;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class MetaConfigProcessor {

    /**
     * Resolves the key to the value entry of the given meta config.
     *
     * @param metaConfig key value set
     * @param key the key to resolve
     * @return the related value of the given key.
     * @throws de.citec.jul.exception.NotAvailableException
     */
    public static String getValue(final MetaConfig metaConfig, final String key) throws NotAvailableException {
        for (EntryType.Entry entry : metaConfig.getEntryList()) {
            if (entry.getKey().equals(key)) {
                if (!entry.getValue().isEmpty()) {
                    return entry.getValue();
                }
            }
        }
        throw new NotAvailableException("value for Key[" + key + "]");
    }
    
    
    public static MetaConfig setValue(final MetaConfig metaConfig, final String key, final String value) throws CouldNotPerformException {
        //TODO mpohling!!!
        return metaConfig;
    }
}
