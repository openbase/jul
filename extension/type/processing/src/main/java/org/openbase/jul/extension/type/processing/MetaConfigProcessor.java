package org.openbase.jul.extension.type.processing;

/*
 * #%L
 * JUL Extension Type Processing
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import java.util.HashMap;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.type.configuration.EntryType;
import org.openbase.type.configuration.EntryType.Entry;
import org.openbase.type.configuration.MetaConfigType.MetaConfig;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class MetaConfigProcessor {

    /**
     * Resolves the key to the value entry of the given meta config.
     *
     * @param metaConfig key value set
     * @param key the key to resolve
     * @return the related value of the given key.
     * @throws org.openbase.jul.exception.NotAvailableException si thrown if not value for the given key could be resolved.
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

    /**
     * Method resolves all variables whose name contains the given identifier.
     *
     * @param metaConfig the meta config to resolve the variables.
     * @param variableContains the identifier to select the variables.
     * @return a map of the variable name and its current value.
     */
    public static Map<String, String> getValues(final MetaConfig metaConfig, final String variableContains) {
        final Map<String, String> variableSelection = new HashMap<>();
        for (EntryType.Entry entry : metaConfig.getEntryList()) {
            if (entry.getKey().contains(variableContains)) {
                if (!entry.getValue().isEmpty()) {
                    variableSelection.put(entry.getKey(), entry.getValue());
                }
            }
        }
        return variableSelection;
    }

    public static MetaConfig setValue(final MetaConfig metaConfig, final String key, final String value) throws CouldNotPerformException {
        return setValue(metaConfig.toBuilder(), key, value).build();
    }

    public static MetaConfig.Builder setValue(final MetaConfig.Builder metaConfigBuilder, final String key, final String value) throws CouldNotPerformException {

        // remove entry if already exist.
        for (int i = 0; i < metaConfigBuilder.getEntryCount(); i++) {
            if (metaConfigBuilder.getEntry(i).getKey().equals(key)) {
                metaConfigBuilder.removeEntry(i);
                break;
            }
        }

        // add new entry
        metaConfigBuilder.addEntry(Entry.newBuilder().setKey(key).setValue(value));
        return metaConfigBuilder;
    }
}
