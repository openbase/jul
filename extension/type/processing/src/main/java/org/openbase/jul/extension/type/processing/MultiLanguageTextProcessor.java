package org.openbase.jul.extension.type.processing;

/*-
 * #%L
 * JUL Extension Type Processing
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import org.openbase.type.language.MultiLanguageTextType.MultiLanguageText;
import org.openbase.type.language.MultiLanguageTextType.MultiLanguageText.Builder;
import org.openbase.type.language.MultiLanguageTextType.MultiLanguageText.MapFieldEntry;
import org.openbase.type.language.MultiLanguageTextType.MultiLanguageTextOrBuilder;

import java.util.Locale;

/**
 * The MultiLanguageTextProcessor is a helper class which makes dealing with {@link org.openbase.type.language.MultiLanguageTextType.MultiLanguageText}
 * easier.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class MultiLanguageTextProcessor {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiLanguageTextProcessor.class);

    /**
     * Test if the multiLanguageText is empty. This means that every multiLanguageText list for every
     * languageCode is empty or that it only contains empty string.
     *
     * @param multiLanguageText the multiLanguageText type which is tested
     *
     * @return if the multiLanguageText type is empty as explained above
     */
    public static boolean isEmpty(final MultiLanguageText multiLanguageText) {
        for (MultiLanguageText.MapFieldEntry entry : multiLanguageText.getEntryList()) {
            if (!entry.getValue().isEmpty()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Create a new multiLanguageTextBuilder and register multiLanguageText with the default locale.
     *
     * @param multiLanguageText the multiLanguageText to be added
     *
     * @return the updated multiLanguageText builder
     */
    public static MultiLanguageText.Builder generateMultiLanguageTextBuilder(final String multiLanguageText) {
        return addMultiLanguageText(MultiLanguageText.newBuilder(), Locale.getDefault(), multiLanguageText);
    }

    /**
     * Create a new multiLanguageTextBuilder and register multiLanguageText with given locale.
     *
     * @param locale      the locale from which the language code is extracted for which the multiLanguageText is added
     * @param multiLanguageText the multiLanguageText to be added
     *
     * @return the updated multiLanguageText builder
     */
    public static MultiLanguageText.Builder generateMultiLanguageTextBuilder(final Locale locale, final String multiLanguageText) {
        return addMultiLanguageText(MultiLanguageText.newBuilder(), locale.getLanguage(), multiLanguageText);
    }

    /**
     * Create a new multiLanguageTextBuilder and register multiLanguageText with the default locale.
     *
     * @param multiLanguageText the multiLanguageText to be added
     *
     * @return the updated multiLanguageText builder
     */
    public static MultiLanguageText buildMultiLanguageText(final String multiLanguageText) {
        return addMultiLanguageText(MultiLanguageText.newBuilder(), Locale.getDefault(), multiLanguageText).build();
    }

    /**
     * Create a new multiLanguageTextBuilder and register multiLanguageText with given locale.
     *
     * @param locale      the locale from which the language code is extracted for which the multiLanguageText is added
     * @param multiLanguageText the multiLanguageText to be added
     *
     * @return the updated multiLanguageText builder
     */
    public static MultiLanguageText buildMultiLanguageText(final Locale locale, final String multiLanguageText) {
        return addMultiLanguageText(MultiLanguageText.newBuilder(), locale.getLanguage(), multiLanguageText).build();
    }

    /**
     * Add a multiLanguageText to a multiLanguageTextBuilder by locale. This is equivalent to calling
     * {@link #addMultiLanguageText(Builder, String, String)} but the language code is extracted from the locale
     * by calling {@link Locale#getLanguage()}.
     *
     * @param multiLanguageTextBuilder the multiLanguageText builder to be updated
     * @param locale             the locale from which the language code is extracted for which the multiLanguageText is added
     * @param multiLanguageText        the multiLanguageText to be added
     *
     * @return the updated multiLanguageText builder
     */
    public static MultiLanguageText.Builder addMultiLanguageText(final MultiLanguageText.Builder multiLanguageTextBuilder, final Locale locale, final String multiLanguageText) {
        return addMultiLanguageText(multiLanguageTextBuilder, locale.getLanguage(), multiLanguageText);
    }

    /**
     * Add a multiLanguageText to a multiLanguageTextBuilder by languageCode. If the multiLanguageText is already contained for the
     * language code nothing will be done. If the languageCode already exists and the multiLanguageText is not contained
     * it will be added to the end of the multiLanguageText list by this language code. If no entry for the languageCode
     * exists a new entry will be added and the multiLanguageText added as its first value.
     *
     * @param multiLanguageTextBuilder the multiLanguageText builder to be updated
     * @param languageCode       the languageCode for which the multiLanguageText is added
     * @param multiLanguageText        the multiLanguageText to be added
     *
     * @return the updated multiLanguageText builder
     */
    public static MultiLanguageText.Builder addMultiLanguageText(final MultiLanguageText.Builder multiLanguageTextBuilder, final String languageCode, final String multiLanguageText) {
        for (int i = 0; i < multiLanguageTextBuilder.getEntryCount(); i++) {
            // found multiLanguageTexts for the entry key
            if (multiLanguageTextBuilder.getEntry(i).getKey().equals(languageCode)) {
                // check if the new value is not already contained
                if (multiLanguageTextBuilder.getEntryBuilder(i).getValue().equalsIgnoreCase(multiLanguageText)) {
                    // return because multiLanguageText is already in there
                    return multiLanguageTextBuilder;
                }
                // add new multiLanguageText
                multiLanguageTextBuilder.getEntryBuilder(i).setValue(multiLanguageText);
                return multiLanguageTextBuilder;
            }
        }

        // language code not present yet
        multiLanguageTextBuilder.addEntryBuilder().setKey(languageCode).setValue(multiLanguageText);
        return multiLanguageTextBuilder;
    }

    /**
     * Get the first multiLanguageText found in the multiLanguageText type. This is independent of the language of the multiLanguageText.
     *
     * @param multiLanguageText the multiLanguageText type which is searched for the first multiLanguageText
     *
     * @return the first multiLanguageText found
     *
     * @throws NotAvailableException if now multiLanguageText is contained in the multiLanguageText type.
     */
    public static String getFirstMultiLanguageText(final MultiLanguageTextOrBuilder multiLanguageText) throws NotAvailableException {
        for (MultiLanguageText.MapFieldEntry entry : multiLanguageText.getEntryList()) {
            return entry.getValue();
        }
        throw new NotAvailableException("MultiLanguageText");
    }

    /**
     * Get the first multiLanguageText for a languageCode from a multiLanguageText type. This is equivalent to calling
     * {@link #getMultiLanguageTextByLanguage(String, MultiLanguageTextOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getLanguage()}. If no multiLanguageText matches the languageCode, than the first multiLanguageText of any other provided language is returned.
     *
     * @param locale      the locale from which a language code is extracted
     * @param multiLanguageText the multiLanguageText type which is searched for multiLanguageTexts in the language
     *
     * @return the first multiLanguageText from the multiLanguageText type for the locale
     *
     * @throws NotAvailableException if no multiLanguageText is provided by the {@code multiLanguageText} argument.
     */
    public static String getBestMatch(final Locale locale, final MultiLanguageTextOrBuilder multiLanguageText) throws NotAvailableException {
        try {
            // resolve multiLanguageText via preferred locale.
            return getMultiLanguageTextByLanguage(locale.getLanguage(), multiLanguageText);
        } catch (NotAvailableException ex) {
            try {
                // resolve world language multiLanguageText.
                return getMultiLanguageTextByLanguage(Locale.ENGLISH, multiLanguageText);
            } catch (NotAvailableException exx) {
                // resolve any multiLanguageText.
                return getFirstMultiLanguageText(multiLanguageText);
            }
        }
    }

    /**
     * Get the first multiLanguageText for the default language from a multiLanguageText type. This is equivalent to calling
     * {@link #getMultiLanguageTextByLanguage(String, MultiLanguageTextOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getDefault()} . If no multiLanguageText matches the languageCode, than the first multiLanguageText of any other provided language is returned.
     *
     * @param multiLanguageText the multiLanguageText type which is searched for multiLanguageTexts in the language
     *
     * @return the first multiLanguageText from the multiLanguageText type for the locale
     *
     * @throws NotAvailableException if no multiLanguageText is provided by the {@code multiLanguageText} argument.
     */
    public static String getBestMatch(final MultiLanguageTextOrBuilder multiLanguageText) throws NotAvailableException {
        return getBestMatch(Locale.getDefault(), multiLanguageText);
    }

    /**
     * Get the first multiLanguageText for the default language from a multiLanguageText type. This is equivalent to calling
     * {@link #getMultiLanguageTextByLanguage(String, MultiLanguageTextOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getDefault()} . If no multiLanguageText matches the languageCode, than the first multiLanguageText of any other provided language is returned.
     *
     * @param multiLanguageText the multiLanguageText type which is searched for multiLanguageTexts in the language
     * @param alternative an alternative string which is returned in error case.
     *
     * @return the first multiLanguageText from the multiLanguageText type for the locale or if no multiLanguageText is provided by the {@code multiLanguageText} argument the {@code alternative} is returned.
     */
    public static String getBestMatch(final MultiLanguageTextOrBuilder multiLanguageText, final String alternative) {
        try {
            return getBestMatch(Locale.getDefault(), multiLanguageText);
        } catch (NotAvailableException e) {
            return alternative;
        }
    }

    /**
     * Get the multiLanguageText for a locale in a multiLanguageText type. This is equivalent to calling
     * {@link #getMultiLanguageTextByLanguage(String, MultiLanguageTextOrBuilder)} but the language code is extracted from the locale by calling
     * {@link Locale#getLanguage()}.
     *
     * @param locale      the locale from which the language code is extracted
     * @param multiLanguageText the multiLanguageText type that is searched for a multiLanguageText list
     *
     * @return the multiLanguageText for the locale in the language type
     *
     * @throws NotAvailableException if no entry for the locale exist in the multiLanguageText type
     */
    public static String getMultiLanguageTextByLanguage(final Locale locale, final MultiLanguageTextOrBuilder multiLanguageText) throws NotAvailableException {
        return getMultiLanguageTextByLanguage(locale.getLanguage(), multiLanguageText);
    }

    /**
     * Get the multiLanguageText for a languageCode in a multiLanguageText type. This is done by checking if the key matches the languageCode. If it matches the value list is
     * returned. Thus the returned list can be empty depending on the multiLanguageText.
     *
     * @param languageCode the languageCode which is checked
     * @param multiLanguageText  the multiLanguageText type that is searched for a multiLanguageText list
     *
     * @return the multiLanguageText for the languageCode in the language type
     *
     * @throws NotAvailableException if no entry for the languageCode exist in the multiLanguageText type
     */
    public static String getMultiLanguageTextByLanguage(final String languageCode, final MultiLanguageTextOrBuilder multiLanguageText) throws NotAvailableException {
        for (MultiLanguageText.MapFieldEntry entry : multiLanguageText.getEntryList()) {
            if (entry.getKey().equalsIgnoreCase(languageCode)) {
                return entry.getValue();
            }
        }
        throw new NotAvailableException("MultiLanguageTextList of Language[" + languageCode + "] in multiLanguageTextType[" + multiLanguageText + "]");
    }

    /**
     * Format the given multiLanguageText by removing duplicated white spaces, underscores and camel cases in all entries.
     *
     * @param multiLanguageText the multiLanguageText to format.
     *
     * @return the formatted multiLanguageText.
     */
    public static MultiLanguageText.Builder format(final MultiLanguageText.Builder multiLanguageText) {
        for (final MapFieldEntry.Builder entryBuilder : multiLanguageText.getEntryBuilderList()) {
            final String value = entryBuilder.getValue();
            entryBuilder.clearValue();
            entryBuilder.setValue(format(value));
        }
        return multiLanguageText;
    }

    /**
     * Format the given multiLanguageText by removing duplicated white spaces, underscores and camel cases.
     *
     * @param multiLanguageText the multiLanguageText to format.
     *
     * @return the formatted multiLanguageText.
     */
    public static String format(String multiLanguageText) {
        if (multiLanguageText.isEmpty()) {
            return multiLanguageText;
        }
        if (Character.isDigit(multiLanguageText.charAt(multiLanguageText.length() - 1))) {
            for (int i = multiLanguageText.length(); i > 0; i--) {
                if (!Character.isDigit(multiLanguageText.charAt(i - 1))) {
                    if (!Character.isLowerCase(multiLanguageText.charAt(i - 1))) {
                        break;
                    }
                    multiLanguageText = multiLanguageText.substring(0, i) + " " + multiLanguageText.substring(i);
                    break;
                }
            }
        }
        return StringProcessor.formatHumanReadable(multiLanguageText);
    }
}
