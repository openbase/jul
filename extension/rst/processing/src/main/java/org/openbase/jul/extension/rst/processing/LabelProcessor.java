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

import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.configuration.LabelType.Label;
import rst.configuration.LabelType.Label.Builder;
import rst.configuration.LabelType.LabelOrBuilder;

import java.util.List;
import java.util.Locale;

/**
 * The LabelProcessor is a helper class which makes dealing with {@link rst.configuration.LabelType.Label}
 * easier.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class LabelProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(LabelProcessor.class);

    /**
     * Test if a label contains a label string. This test iterates over all languages and all
     * label strings for the language and check if one equals the label string.
     * The test is done ignoring the case of the label string.
     *
     * @param label       the label type which is checked
     * @param labelString the label string that is tested if it is contained in the label type
     * @return if the labelString is contained in the label type
     */
    public static boolean contains(final Label label, final String labelString) {
        for (Label.MapFieldEntry entry : label.getEntryList()) {
            for (String value : entry.getValueList()) {
                if (value.equalsIgnoreCase(labelString)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Test if the label is empty. This means that every label list for every
     * languageCode is empty or that it only contains empty string.
     *
     * @param label the label type which is tested
     * @return if the label type is empty as explained above
     */
    public static boolean isEmpty(final Label label) {
        for (Label.MapFieldEntry entry : label.getEntryList()) {
            for (String value : entry.getValueList()) {
                if (!value.isEmpty()) {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Add a label to a labelBuilder by locale. This is equivalent to calling
     * {@link #addLabel(Builder, String, String)} but the language code is extracted from the locale
     * by calling {@link Locale#getLanguage()}.
     *
     * @param labelBuilder the label builder to be updated
     * @param locale       the locale from which the language code is extracted for which the label is added
     * @param label        the label to be added
     * @return the updated label builder
     */
    public static Label.Builder addLabel(final Label.Builder labelBuilder, final Locale locale, final String label) {
        return addLabel(labelBuilder, locale.getLanguage(), label);
    }

    /**
     * Add a label to a labelBuilder by languageCode. If the label is already contained for the
     * language code nothing will be done. If the languageCode already exists and the label is not contained
     * it will be added to the end of the label list by this language code. If no entry for the languageCode
     * exists a new entry will be added and the label added as its first value.
     *
     * @param labelBuilder the label builder to be updated
     * @param languageCode the languageCode for which the label is added
     * @param label        the label to be added
     * @return the updated label builder
     */
    public static Label.Builder addLabel(final Label.Builder labelBuilder, final String languageCode, final String label) {
        for (int i = 0; i < labelBuilder.getEntryCount(); i++) {
            // found labels for the entry key
            if (labelBuilder.getEntry(i).getKey().equals(languageCode)) {
                // check if the new value is not already contained
                for (String value : labelBuilder.getEntryBuilder(i).getValueList()) {
                    if (value.equalsIgnoreCase(label)) {
                        // return because label is already in there
                        return labelBuilder;
                    }
                }

                // add new label
                labelBuilder.getEntryBuilder(i).addValue(label);
                return labelBuilder;
            }
        }

        // language code not present yet
        labelBuilder.addEntryBuilder().setKey(languageCode).addValue(label);
        return labelBuilder;
    }

    /**
     * Get the first label found in the label type. This is independent of the language of the label.
     *
     * @param label the label type which is searched for the first label
     * @return the first label found
     * @throws NotAvailableException if now label is contained in the label type.
     */
    public static String getFirstLabel(final LabelOrBuilder label) throws NotAvailableException {
        for (Label.MapFieldEntry entry : label.getEntryList()) {
            for (String value : entry.getValueList()) {
                return value;
            }
        }
        throw new NotAvailableException("No label available");
    }

    /**
     * Get the first label for a languageCode from a label type. This is equivalent to calling
     * {@link #getLabelByLanguage(String, LabelOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getLanguage()}.
     *
     * @param locale the locale from which a language code is extracted
     * @param label  the label type which is searched for labels in the language
     * @return the first label from the label type for the locale
     * @throws NotAvailableException if no label list for the locale exists of if the list is empty
     */
    public static String getLabelByLanguage(final Locale locale, final LabelOrBuilder label) throws NotAvailableException {
        return getLabelByLanguage(locale.getLanguage(), label);
    }

    /**
     * Get the first label for a languageCode from a label type.
     * This method will call {@link #getLabelListByLanguage(String, LabelOrBuilder)} to extract the list of labels
     * for the languageCode and then return its first entry.
     *
     * @param languageCode the languageCode which is checked
     * @param label        the label type which is searched for labels in the language
     * @return the first label from the label type for the language code.
     * @throws NotAvailableException if no label list for the language code exists or if the list is empty
     */
    public static String getLabelByLanguage(final String languageCode, final LabelOrBuilder label) throws NotAvailableException {
        final List<String> labelList = getLabelListByLanguage(languageCode, label);
        if (labelList.isEmpty()) {
            throw new NotAvailableException("Label for language[" + languageCode + "]");
        }
        return labelList.get(0);
    }

    /**
     * Get a list of all labels for a locale in a label type. This is equivalent to calling
     * {@link #getLabelListByLanguage(String, LabelOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getLanguage()}.
     *
     * @param locale the locale from which the language code is extracted
     * @param label  the label type that is searched for a label list
     * @return a list of all labels for the locale in the language type
     * @throws NotAvailableException if no entry for the locale exist in the label type
     */
    public static List<String> getLabelListByLanguage(final Locale locale, final LabelOrBuilder label) throws NotAvailableException {
        return getLabelListByLanguage(locale.getLanguage(), label);
    }

    /**
     * Get a list of all labels for a languageCode in a label type. This is done by iterating over all entries
     * in the label type and checking if the key matches the languageCode. If it matches the value list is
     * returned. Thus the returned list can be empty depending on the label.
     *
     * @param languageCode the languageCode which is checked
     * @param label        the label type that is searched for a label list
     * @return a list of all labels for the languageCode in the language type
     * @throws NotAvailableException if no entry for the languageCode exist in the label type
     */
    public static List<String> getLabelListByLanguage(final String languageCode, final LabelOrBuilder label) throws NotAvailableException {
        for (Label.MapFieldEntry entry : label.getEntryList()) {
            if (entry.getKey().equalsIgnoreCase(languageCode)) {
                return entry.getValueList();
            }
        }
        throw new NotAvailableException("LabelList of language[" + languageCode + "] in labelType[" + label + "]");
    }
}
