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
import org.openbase.jul.processing.StringProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rst.language.DescriptionType.Description;
import rst.language.DescriptionType.Description.Builder;
import rst.language.DescriptionType.Description.MapFieldEntry;
import rst.language.DescriptionType.DescriptionOrBuilder;

import java.util.Locale;

/**
 * The DescriptionProcessor is a helper class which makes dealing with {@link rst.language.DescriptionType.Description}
 * easier.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class DescriptionProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(DescriptionProcessor.class);

    /**
     * Test if the description is empty. This means that every description list for every
     * languageCode is empty or that it only contains empty string.
     *
     * @param description the description type which is tested
     *
     * @return if the description type is empty as explained above
     */
    public static boolean isEmpty(final Description description) {
        for (Description.MapFieldEntry entry : description.getEntryList()) {
            if (!entry.getValue().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new descriptionBuilder and register description with the default locale.
     *
     * @param description the description to be added
     *
     * @return the updated description builder
     */
    public static Description.Builder generateDescriptionBuilder(final String description) {
        return addDescription(Description.newBuilder(), Locale.getDefault(), description);
    }

    /**
     * Create a new descriptionBuilder and register description with given locale.
     *
     * @param locale      the locale from which the language code is extracted for which the description is added
     * @param description the description to be added
     *
     * @return the updated description builder
     */
    public static Description.Builder generateDescriptionBuilder(final Locale locale, final String description) {
        return addDescription(Description.newBuilder(), locale.getLanguage(), description);
    }

    /**
     * Create a new descriptionBuilder and register description with the default locale.
     *
     * @param description the description to be added
     *
     * @return the updated description builder
     */
    public static Description buildDescription(final String description) {
        return addDescription(Description.newBuilder(), Locale.getDefault(), description).build();
    }

    /**
     * Create a new descriptionBuilder and register description with given locale.
     *
     * @param locale      the locale from which the language code is extracted for which the description is added
     * @param description the description to be added
     *
     * @return the updated description builder
     */
    public static Description buildDescription(final Locale locale, final String description) {
        return addDescription(Description.newBuilder(), locale.getLanguage(), description).build();
    }

    /**
     * Add a description to a descriptionBuilder by locale. This is equivalent to calling
     * {@link #addDescription(Builder, String, String)} but the language code is extracted from the locale
     * by calling {@link Locale#getLanguage()}.
     *
     * @param descriptionBuilder the description builder to be updated
     * @param locale             the locale from which the language code is extracted for which the description is added
     * @param description        the description to be added
     *
     * @return the updated description builder
     */
    public static Description.Builder addDescription(final Description.Builder descriptionBuilder, final Locale locale, final String description) {
        return addDescription(descriptionBuilder, locale.getLanguage(), description);
    }

    /**
     * Add a description to a descriptionBuilder by languageCode. If the description is already contained for the
     * language code nothing will be done. If the languageCode already exists and the description is not contained
     * it will be added to the end of the description list by this language code. If no entry for the languageCode
     * exists a new entry will be added and the description added as its first value.
     *
     * @param descriptionBuilder the description builder to be updated
     * @param languageCode       the languageCode for which the description is added
     * @param description        the description to be added
     *
     * @return the updated description builder
     */
    public static Description.Builder addDescription(final Description.Builder descriptionBuilder, final String languageCode, final String description) {
        for (int i = 0; i < descriptionBuilder.getEntryCount(); i++) {
            // found descriptions for the entry key
            if (descriptionBuilder.getEntry(i).getKey().equals(languageCode)) {
                // check if the new value is not already contained
                if (descriptionBuilder.getEntryBuilder(i).getValue().equalsIgnoreCase(description)) {
                    // return because description is already in there
                    return descriptionBuilder;
                }
                // add new description
                descriptionBuilder.getEntryBuilder(i).setValue(description);
                return descriptionBuilder;
            }
        }

        // language code not present yet
        descriptionBuilder.addEntryBuilder().setKey(languageCode).setValue(description);
        return descriptionBuilder;
    }

    /**
     * Get the first description found in the description type. This is independent of the language of the description.
     *
     * @param description the description type which is searched for the first description
     *
     * @return the first description found
     *
     * @throws NotAvailableException if now description is contained in the description type.
     */
    public static String getFirstDescription(final DescriptionOrBuilder description) throws NotAvailableException {
        for (Description.MapFieldEntry entry : description.getEntryList()) {
            return entry.getValue();
        }
        throw new NotAvailableException("Description");
    }

    /**
     * Get the first description for a languageCode from a description type. This is equivalent to calling
     * {@link #getDescriptionByLanguage(String, DescriptionOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getLanguage()}. If no description matches the languageCode, than the first description of any other provided language is returned.
     *
     * @param locale      the locale from which a language code is extracted
     * @param description the description type which is searched for descriptions in the language
     *
     * @return the first description from the description type for the locale
     *
     * @throws NotAvailableException if no description is provided by the {@code description} argument.
     */
    public static String getBestMatch(final Locale locale, final DescriptionOrBuilder description) throws NotAvailableException {
        try {
            // resolve description via preferred locale.
            return getDescriptionByLanguage(locale.getLanguage(), description);
        } catch (NotAvailableException ex) {
            try {
                // resolve world language description.
                return getDescriptionByLanguage(Locale.ENGLISH, description);
            } catch (NotAvailableException exx) {
                // resolve any description.
                return getFirstDescription(description);
            }
        }
    }

    /**
     * Get the first description for the default language from a description type. This is equivalent to calling
     * {@link #getDescriptionByLanguage(String, DescriptionOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getDefault()} . If no description matches the languageCode, than the first description of any other provided language is returned.
     *
     * @param description the description type which is searched for descriptions in the language
     *
     * @return the first description from the description type for the locale
     *
     * @throws NotAvailableException if no description is provided by the {@code description} argument.
     */
    public static String getBestMatch(final DescriptionOrBuilder description) throws NotAvailableException {
        return getBestMatch(Locale.getDefault(), description);
    }

    /**
     * Get the first description for the default language from a description type. This is equivalent to calling
     * {@link #getDescriptionByLanguage(String, DescriptionOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getDefault()} . If no description matches the languageCode, than the first description of any other provided language is returned.
     *
     * @param description the description type which is searched for descriptions in the language
     * @param alternative an alternative string which is returned in error case.
     *
     * @return the first description from the description type for the locale or if no description is provided by the {@code description} argument the {@code alternative} is returned.
     */
    public static String getBestMatch(final DescriptionOrBuilder description, final String alternative) {
        try {
            return getBestMatch(Locale.getDefault(), description);
        } catch (NotAvailableException e) {
            return alternative;
        }
    }

    /**
     * Get the description for a locale in a description type. This is equivalent to calling
     * {@link #getDescriptionByLanguage(String, DescriptionOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getLanguage()}.
     *
     * @param locale      the locale from which the language code is extracted
     * @param description the description type that is searched for a description list
     *
     * @return the description for the locale in the language type
     *
     * @throws NotAvailableException if no entry for the locale exist in the description type
     */
    public static String getDescriptionByLanguage(final Locale locale, final DescriptionOrBuilder description) throws NotAvailableException {
        return getDescriptionByLanguage(locale.getLanguage(), description);
    }

    /**
     * Get the description for a languageCode in a description type. This is done by checking if the key matches the languageCode. If it matches the value list is
     * returned. Thus the returned list can be empty depending on the description.
     *
     * @param languageCode the languageCode which is checked
     * @param description  the description type that is searched for a description list
     *
     * @return the description for the languageCode in the language type
     *
     * @throws NotAvailableException if no entry for the languageCode exist in the description type
     */
    public static String getDescriptionByLanguage(final String languageCode, final DescriptionOrBuilder description) throws NotAvailableException {
        for (Description.MapFieldEntry entry : description.getEntryList()) {
            if (entry.getKey().equalsIgnoreCase(languageCode)) {
                return entry.getValue();
            }
        }
        throw new NotAvailableException("DescriptionList of Language[" + languageCode + "] in descriptionType[" + description + "]");
    }

    /**
     * Format the given description by removing duplicated white spaces, underscores and camel cases in all entries.
     *
     * @param description the description to format.
     *
     * @return the formatted description.
     */
    public static Description.Builder format(final Description.Builder description) {
        for (final MapFieldEntry.Builder entryBuilder : description.getEntryBuilderList()) {
            final String value = entryBuilder.getValue();
            entryBuilder.clearValue();
            entryBuilder.setValue(format(value));
        }
        return description;
    }

    /**
     * Format the given description by removing duplicated white spaces, underscores and camel cases.
     *
     * @param description the description to format.
     *
     * @return the formatted description.
     */
    public static String format(String description) {
        if (description.isEmpty()) {
            return description;
        }
        if (Character.isDigit(description.charAt(description.length() - 1))) {
            for (int i = description.length(); i > 0; i--) {
                if (!Character.isDigit(description.charAt(i - 1))) {
                    if (!Character.isLowerCase(description.charAt(i - 1))) {
                        break;
                    }
                    description = description.substring(0, i) + " " + description.substring(i);
                    break;
                }
            }
        }
        return StringProcessor.formatHumanReadable(description);
    }
}
