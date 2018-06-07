package org.openbase.jul.extension.rst.processing;

/*-
 * #%L
 * JUL Extension RST Processing
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.MessageOrBuilder;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import rst.configuration.LabelType.Label;

import java.util.List;
import java.util.Locale;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class LabelProcessor {

    private static final FieldDescriptor ENTRY_FIELD = Label.getDescriptor().findFieldByNumber(Label.ENTRY_FIELD_NUMBER);

    public static String getFirstLabel(final Label label) throws NotAvailableException {
        for (Label.MapFieldEntry entry : label.getEntryList()) {
            for (String value : entry.getValueList()) {
                return value;
            }
        }
        throw new NotAvailableException("No label available");
    }

    public static String getLabelByLanguage(final Locale locale, final Label label) throws CouldNotPerformException {
        return getLabelByLanguage(locale.getLanguage(), label);
    }

    public static String getLabelByLanguage(final String languageCode, final Label label) throws CouldNotPerformException {
        final List<String> labelList = getLabelListByLanguage(languageCode, label);
        if (labelList.isEmpty()) {
            throw new NotAvailableException("Label for language[" + languageCode + "]");
        }
        return labelList.get(0);
    }

    public static List<String> getLabelListByLanguage(final Locale locale, final Label label) throws CouldNotPerformException {
        return getLabelListByLanguage(locale.getLanguage(), label);
    }

    @SuppressWarnings("unchecked")
    public static List<String> getLabelListByLanguage(final String languageCode, final Label label) throws CouldNotPerformException {
        return (List<String>) getMapValue(languageCode, ENTRY_FIELD, label, List.class);
    }

    public static <VT> VT getMapValue(final Object key, final FieldDescriptor mapFieldDescriptor, final MessageOrBuilder mapHolder, final Class<VT> valueClass) throws CouldNotPerformException {
        try {
            FieldDescriptor keyDescriptor = null;
            FieldDescriptor valueDescriptor = null;
            for (int i = 0; i < mapHolder.getRepeatedFieldCount(mapFieldDescriptor); i++) {
                final Message entry = (Message) mapHolder.getRepeatedField(mapFieldDescriptor, i);

                if (keyDescriptor == null || valueDescriptor == null) {
                    keyDescriptor = entry.getDescriptorForType().findFieldByName("KEY");
                    valueDescriptor = entry.getDescriptorForType().findFieldByName("VALUE");
                }

                if (entry.getField(keyDescriptor).equals(key)) {
                    return (VT) entry.getField(valueDescriptor);
                }
            }
            throw new NotAvailableException("Entry with key[" + key + "]");
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not get value from map", ex);
        }
    }
}
