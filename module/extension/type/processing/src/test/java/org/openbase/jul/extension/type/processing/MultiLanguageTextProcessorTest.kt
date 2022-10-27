package org.openbase.jul.extension.type.processing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.util.*

class MultiLanguageTextProcessorTest {

    @Test
    fun `test adding multiple values for the same language`() {
        val text1 = "this is a nice sample text"
        val text2 = "this sample text is even nicer"
        val locale = Locale.ENGLISH

        // create the multi-language text
        val multiLanguageText =
            MultiLanguageTextProcessor.generateMultiLanguageTextBuilder(locale, text1)
        Assertions.assertEquals(1, multiLanguageText.entryList.size)
        Assertions.assertEquals(text1, MultiLanguageTextProcessor.getBestMatch(locale, multiLanguageText))

        // try to add the same text again
        MultiLanguageTextProcessor.addMultiLanguageText(multiLanguageText, locale, text1)
        Assertions.assertEquals(1, multiLanguageText.entryList.size)
        Assertions.assertEquals(text1, MultiLanguageTextProcessor.getBestMatch(locale, multiLanguageText))

        // try if a different text with the same language replaces the first one
        MultiLanguageTextProcessor.addMultiLanguageText(multiLanguageText, locale, text2)
        Assertions.assertEquals(1, multiLanguageText.entryList.size)
        Assertions.assertEquals(text2, MultiLanguageTextProcessor.getBestMatch(locale, multiLanguageText))
    }
}
