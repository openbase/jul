package org.openbase.jul.extension.type.processing

import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.processing.StringProcessor
import org.openbase.type.language.LabelType
import org.slf4j.LoggerFactory
import java.util.*

/*-
 * #%L
 * JUL Extension Type Processing
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
 */ /**
 * The LabelProcessor is a helper class which makes dealing with [org.openbase.type.language.LabelType.Label]
 * easier.
 *
 * @author [Tamino Huxohl](mailto:pleminoq@openbase.org)
 */
object LabelProcessor {

    const val LANGUAGE_CODE_TECHNICAL = "technical"

    @JvmField
    val UNKNOWN_LABEL = buildLabel(Locale.ENGLISH, "?")
    private val LOGGER = LoggerFactory.getLogger(LabelProcessor::class.java)

    /**
     * Test if a label contains a label string. This test iterates over all languages and all
     * label strings for the language and check if one equals the label string.
     * The test is done ignoring the case of the label string and ignoring white spaces.
     *
     * @param label       the label type which is checked
     * @param labelString the label string that is tested if it is contained in the label type
     *
     * @return if the labelString is contained in the label type
     */
    @JvmStatic
    fun contains(label: LabelType.Label, labelString: String): Boolean {
        val withoutWhite = StringProcessor.removeWhiteSpaces(format(labelString))
        for (entry in label.entryList) {
            for (value in entry.valueList) {
                if (StringProcessor.removeWhiteSpaces(format(value)).equals(withoutWhite, ignoreCase = true)) {
                    return true
                }
            }
        }
        return false
    }

    /**
     * Test if the label is empty. This means that every label list for every
     * languageCode is empty or that it only contains empty string.
     *
     * @param label the label type which is tested
     *
     * @return if the label type is empty as explained above
     */
    @JvmStatic
    fun isEmpty(label: LabelType.Label): Boolean {
        for (entry in label.entryList) {
            for (value in entry.valueList) {
                if (!value.isEmpty()) {
                    return false
                }
            }
        }
        return true
    }

    /**
     * Create a new labelBuilder and register label with the default locale.
     *
     * @param label the label to be added
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun generateLabelBuilder(label: String?): LabelType.Label.Builder {
        return addLabel(LabelType.Label.newBuilder(), Locale.getDefault(), label)
    }

    /**
     * Create a new labelBuilder and register label with given locale.
     *
     * @param locale the locale from which the language code is extracted for which the label is added
     * @param label  the label to be added
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun generateLabelBuilder(locale: Locale, label: String?): LabelType.Label.Builder {
        return addLabel(LabelType.Label.newBuilder(), locale.language, label)
    }

    /**
     * Create a new labelBuilder and register label with the default locale.
     *
     * @param label the label to be added
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun buildLabel(label: String?): LabelType.Label {
        return addLabel(LabelType.Label.newBuilder(), Locale.getDefault(), label).build()
    }

    /**
     * Create a new labelBuilder and register label with given locale.
     *
     * @param locale the locale from which the language code is extracted for which the label is added
     * @param label  the label to be added
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun buildLabel(locale: Locale, label: String?): LabelType.Label {
        return addLabel(LabelType.Label.newBuilder(), locale.language, label).build()
    }

    /**
     * Add a label to a labelBuilder by locale. This is equivalent to calling
     * [.addLabel] but the language code is extracted from the locale
     * by calling [Locale.getLanguage].
     *
     * @param labelBuilder the label builder to be updated
     * @param locale       the locale from which the language code is extracted for which the label is added
     * @param label        the label to be added
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun addLabel(labelBuilder: LabelType.Label.Builder, locale: Locale, label: String?): LabelType.Label.Builder {
        return addLabel(labelBuilder, locale.language, label)
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
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun addLabel(
        labelBuilder: LabelType.Label.Builder,
        languageCode: String,
        label: String?
    ): LabelType.Label.Builder {
        for (i in 0 until labelBuilder.entryCount) {
            // found labels for the entry key
            if (labelBuilder.getEntry(i).key == languageCode) {
                // check if the new value is not already contained
                for (value in labelBuilder.getEntryBuilder(i).valueList) {
                    if (value.equals(label, ignoreCase = true)) {
                        // return because label is already in there
                        return labelBuilder
                    }
                }

                // add new label
                labelBuilder.getEntryBuilder(i).addValue(label)
                return labelBuilder
            }
        }

        // language code not present yet
        labelBuilder.addEntryBuilder().setKey(languageCode).addValue(label)
        return labelBuilder
    }

    /**
     * Get the first label found in the label type. This is independent of the language of the label.
     *
     * @param label the label type which is searched for the first label
     *
     * @return the first label found
     *
     * @throws NotAvailableException if now label is contained in the label type.
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getFirstLabel(label: LabelType.LabelOrBuilder): String? {
        var technical: String? = null
        outer@ for (entry in label.entryList) {
            for (value in entry.valueList) {
                // avoid technical
                if (entry.key == LANGUAGE_CODE_TECHNICAL) {
                    technical = value
                    continue@outer
                }
                return value
            }
        }

        // use technical as last possibility
        if (technical != null) {
            return technical
        }
        throw NotAvailableException("Label")
    }

    /**
     * Get the first label for a languageCode from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getLanguage]. If no label matches the languageCode, than the first label of any other provided language is returned.
     *
     * @param languageCode the language tag from which a language code is extracted
     * @param label        the label type which is searched for labels in the language
     *
     * @return the first label from the label type for the locale
     *
     * @throws NotAvailableException if no label is provided by the `label` argument.
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getBestMatch(languageCode: String, label: LabelType.LabelOrBuilder): String? {
        return try {

            // handle technical case
            if (languageCode == LANGUAGE_CODE_TECHNICAL) {
                getLabelByLanguage(languageCode, label)
            } else getLabelByLanguage(Locale.forLanguageTag(languageCode).language, label)

            // resolve label via preferred locale.
        } catch (ex: NotAvailableException) {
            try {
                // resolve world language label.
                getLabelByLanguage(Locale.ENGLISH, label)
            } catch (exx: NotAvailableException) {
                // resolve any label.
                getFirstLabel(label)
            }
        }
    }

    /**
     * Get the first label for a languageCode from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getLanguage]. If no label matches the languageCode, than the first label of any other provided language is returned.
     *
     * @param locale the locale from which a language code is extracted
     * @param label  the label type which is searched for labels in the language
     *
     * @return the first label from the label type for the locale
     *
     * @throws NotAvailableException if no label is provided by the `label` argument.
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getBestMatch(locale: Locale, label: LabelType.LabelOrBuilder): String? {
        return try {
            // resolve label via preferred locale.
            getLabelByLanguage(locale.language, label)
        } catch (ex: NotAvailableException) {
            try {
                // resolve world language label.
                getLabelByLanguage(Locale.ENGLISH, label)
            } catch (exx: NotAvailableException) {
                // resolve any label.
                getFirstLabel(label)
            }
        }
    }

    /**
     * Get the first label for the default language from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getDefault] . If no label matches the languageCode, than the first label of any other provided language is returned.
     *
     * @param label the label type which is searched for labels in the language
     *
     * @return the first label from the label type for the locale
     *
     * @throws NotAvailableException if no label is provided by the `label` argument.
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getBestMatch(label: LabelType.LabelOrBuilder): String? {
        return getBestMatch(Locale.getDefault(), label)
    }

    /**
     * Get the first label for a languageCode from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getLanguage]. If no label matches the languageCode, than the first label of any other provided language is returned.
     *
     * @param languageCode the language tag from which a language code is extracted
     * @param label        the label type which is searched for labels in the language
     * @param alternative an alternative string which is returned in error case.
     *
     * @return the first label from the label type for the locale
     */
    @JvmStatic
    fun getBestMatch(languageCode: String, label: LabelType.LabelOrBuilder, alternative: String?): String? {
        return try {
            getBestMatch(languageCode, label)
        } catch (e: NotAvailableException) {
            alternative
        }
    }

    /**
     * Get the first label for a languageCode from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getLanguage]. If no label matches the languageCode, than the first label of any other provided language is returned.
     *
     * @param locale the locale from which a language code is extracted
     * @param label  the label type which is searched for labels in the language
     * @param alternative an alternative string which is returned in error case.
     *
     * @return the first label from the label type for the locale
     */
    @JvmStatic
    fun getBestMatch(locale: Locale, label: LabelType.LabelOrBuilder, alternative: String?): String? {
        return try {
            getBestMatch(locale, label)
        } catch (e: NotAvailableException) {
            alternative
        }
    }

    /**
     * Get the first label for the default language from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getDefault] . If no label matches the languageCode, than the first label of any other provided language is returned.
     *
     * @param label       the label type which is searched for labels in the language
     * @param alternative an alternative string which is returned in error case.
     *
     * @return the first label from the label type for the locale or if no label is provided by the `label` argument the `alternative` is returned.
     */
    @JvmStatic
    fun getBestMatch(label: LabelType.LabelOrBuilder, alternative: String?): String? {
        return try {
            getBestMatch(Locale.getDefault(), label)
        } catch (e: NotAvailableException) {
            alternative
        }
    }

    /**
     * Get the first label for a languageCode from a label type. This is equivalent to calling
     * [.getLabelByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getLanguage].
     *
     * @param locale the locale from which a language code is extracted
     * @param label  the label type which is searched for labels in the language
     *
     * @return the first label from the label type for the locale
     *
     * @throws NotAvailableException if no label list for the locale exists of if the list is empty
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getLabelByLanguage(locale: Locale, label: LabelType.LabelOrBuilder): String {
        return getLabelByLanguage(locale.language, label)
    }

    /**
     * Get the first label for a languageCode from a label type.
     * This method will call [.getLabelListByLanguage] to extract the list of labels
     * for the languageCode and then return its first entry.
     *
     * @param languageCode the languageCode which is checked
     * @param label        the label type which is searched for labels in the language
     *
     * @return the first label from the label type for the language code.
     *
     * @throws NotAvailableException if no label list for the language code exists or if the list is empty
     */
    @JvmStatic
    @Throws(NotAvailableException::class)
    fun getLabelByLanguage(languageCode: String, label: LabelType.LabelOrBuilder): String {
        val labelList = getLabelListByLanguage(languageCode, label)
        if (labelList.isEmpty()) {
            throw NotAvailableException("Label for Language[$languageCode]")
        }
        return labelList[0]
    }

    /**
     * Get a list of all labels for a locale in a label type. This is equivalent to calling
     * [.getLabelListByLanguage] but the language code is extracted from the locale by calling
     * [Locale.getLanguage].
     *
     * @param locale the locale from which the language code is extracted
     * @param label  the label type that is searched for a label list
     *
     * @return a list of all labels for the locale in the language type
     *
     * @throws NotAvailableException if no entry for the locale exist in the label type
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getLabelListByLanguage(locale: Locale, label: LabelType.LabelOrBuilder): List<String> {
        return getLabelListByLanguage(locale.language, label)
    }

    /**
     * Get a list of all labels for a languageCode in a label type. This is done by iterating over all entries
     * in the label type and checking if the key matches the languageCode. If it matches the value list is
     * returned. Thus the returned list can be empty depending on the label.
     *
     * @param languageCode the languageCode which is checked
     * @param label        the label type that is searched for a label list
     *
     * @return a list of all labels for the languageCode in the language type
     *
     * @throws NotAvailableException if no entry for the languageCode exist in the label type
     */
    @Throws(NotAvailableException::class)
    @JvmStatic
    fun getLabelListByLanguage(languageCode: String, label: LabelType.LabelOrBuilder): List<String> {
        for (entry in label.entryList) {
            if (entry.key.equals(languageCode, ignoreCase = true)) {
                return entry.valueList
            }
        }
        throw NotAvailableException("LabelList of Language[$languageCode] in labelType[$label]")
    }

    /**
     * Replace all instances of a label string in a label builder. The check for the label string which should be
     * replaced is done ignoring the case.
     *
     * @param label    the label builder in which label string will be replaced
     * @param oldLabel the label string which is replaced
     * @param newLabel the label string replacement
     *
     * @return the updated label builder
     */
    @JvmStatic
    fun replace(label: LabelType.Label.Builder, oldLabel: String?, newLabel: String?): LabelType.Label.Builder {
        for (entryBuilder in label.entryBuilderList) {
            val valueList: List<String> = ArrayList(entryBuilder.valueList)
            entryBuilder.clearValue()
            for (value in valueList) {
                if (StringProcessor.removeWhiteSpaces(value)
                        .equals(StringProcessor.removeWhiteSpaces(oldLabel), ignoreCase = true)
                ) {
                    entryBuilder.addValue(newLabel)
                } else {
                    entryBuilder.addValue(value)
                }
            }
        }
        return label
    }

    /**
     * Format the given label by removing duplicated white spaces, underscores and camel cases in all entries.
     *
     * @param label the label to format.
     *
     * @return the formatted label.
     */
    @JvmStatic
    fun format(label: LabelType.Label.Builder): LabelType.Label.Builder {

        val entryList = label.entryBuilderList.toList()
        label.clearEntry()
        return label.addAllEntry(
            entryList
                .map { entryBuilder ->
                    val valueList: List<String> = entryBuilder.valueList.toList()
                    entryBuilder.clearValue()
                    entryBuilder.addAllValue(valueList
                        .map { format(it) }
                        .filter { it.isNotBlank() }
                    )
                }
                .filter { it.valueList.isNotEmpty() }
                .map { it.build() }
        )
    }

    /**
     * Format the given label by removing duplicated white spaces, underscores and camel cases.
     *
     * @param label the label to format.
     *
     * @return the formatted label.
     */
    @JvmStatic
    fun format(label: String): String {
        var label = label
        if (label.isEmpty()) {
            return label
        }
        if (Character.isDigit(label[label.length - 1])) {
            for (i in label.length downTo 1) {
                if (!Character.isDigit(label[i - 1])) {
                    if (!Character.isLowerCase(label[i - 1])) {
                        break
                    }
                    label = label.substring(0, i) + " " + label.substring(i)
                    break
                }
            }
        }
        return StringProcessor.formatHumanReadable(label)
    }
}
