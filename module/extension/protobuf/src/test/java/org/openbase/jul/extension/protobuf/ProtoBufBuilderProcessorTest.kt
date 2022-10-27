package org.openbase.jul.extension.protobuf

import io.kotest.matchers.comparables.shouldBeEqualComparingTo
import io.kotest.matchers.ints.shouldBeExactly
import org.junit.jupiter.api.Test

import org.openbase.jul.extension.protobuf.ProtoBufBuilderProcessor.clearRepeatedFields
import org.openbase.jul.extension.protobuf.ProtoBufBuilderProcessor.mergeFromWithoutRepeatedFields
import org.openbase.type.domotic.action.ActionDescriptionType.ActionDescription
import org.openbase.type.language.MultiLanguageTextType.MultiLanguageText

internal class ProtoBufBuilderProcessorTest {

    companion object {
        private const val DE = "de"
        private const val EN = "en"
        private const val ES = "es"
        private const val EN_DOG = "dog"
        private const val DE_DOG = "hund"
        private const val ES_DOG = "perro"
    }

    @Test
    fun clearRepeatedFields() {
        ActionDescription.newBuilder().also {
            it.descriptionBuilder.addAllEntry(
                mutableListOf(
                    MultiLanguageText.MapFieldEntry.newBuilder().setKey(DE).setValue(DE_DOG).build(),
                    MultiLanguageText.MapFieldEntry.newBuilder().setKey(EN).setValue(EN_DOG).build(),
                    MultiLanguageText.MapFieldEntry.newBuilder().setKey(ES).setValue(ES_DOG).build(),
                )
            ).build()
        }.clearRepeatedFields().apply {
            descriptionBuilder.entryCount shouldBeExactly 0
        }
    }

    @Test
    fun mergeFromWithoutRepeatedFields() {
        val originBuilder = ActionDescription.newBuilder().also {
            it.descriptionBuilder.addAllEntry(
                mutableListOf(
                    MultiLanguageText.MapFieldEntry.newBuilder().setKey(DE).setValue(DE_DOG).build(),
                    MultiLanguageText.MapFieldEntry.newBuilder().setKey(EN).setValue(EN_DOG).build(),
                )
            ).build()
        }

        val builderToMerge = ActionDescription.newBuilder().also {
            it.descriptionBuilder.addAllEntry(
                mutableListOf(
                    MultiLanguageText.MapFieldEntry.newBuilder().setKey(ES).setValue(ES_DOG).build(),
                )
            ).build()
        }

        originBuilder.mergeFromWithoutRepeatedFields(builderToMerge).apply {
            descriptionBuilder.entryCount shouldBeExactly 2
            descriptionBuilder.entryBuilderList[0].key shouldBeEqualComparingTo DE
            descriptionBuilder.entryBuilderList[0].value shouldBeEqualComparingTo DE_DOG
            descriptionBuilder.entryBuilderList[1].key shouldBeEqualComparingTo EN
            descriptionBuilder.entryBuilderList[1].value shouldBeEqualComparingTo EN_DOG
        }
    }
}
