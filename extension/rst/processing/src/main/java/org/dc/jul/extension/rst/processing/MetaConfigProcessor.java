package org.dc.jul.extension.rst.processing;

/*
 * #%L
 * JUL Extension RST Processing
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import rst.configuration.EntryType;
import rst.configuration.EntryType.Entry;
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
     * @throws org.dc.jul.exception.NotAvailableException
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
        return setValue(metaConfig.toBuilder(), key, value).build();
    }
    
    public static MetaConfig.Builder setValue(final MetaConfig.Builder metaConfigBuilder, final String key, final String value) throws CouldNotPerformException {
        
        // remove entry if already exist.
        for (int i = 0; i < metaConfigBuilder.getEntryCount(); i++) {
            if(metaConfigBuilder.getEntry(i).getKey().equals(key)) {
                metaConfigBuilder.removeEntry(i);
                break;
            }
        }
        
        // add new entry
        metaConfigBuilder.addEntry(Entry.newBuilder().setKey(key).setValue(value));
        return metaConfigBuilder;
    }
}
