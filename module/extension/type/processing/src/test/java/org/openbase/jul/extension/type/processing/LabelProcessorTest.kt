package org.openbase.jul.extension.type.processing

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.openbase.jul.extension.type.processing.LabelProcessor.LANGUAGE_CODE_TECHNICAL
import org.openbase.type.language.LabelType.Label
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
 */
class LabelProcessorTest {

    companion object {
        const val DE = "Maus"
        const val EN = "Mouse"
        const val TECH = "Animal"
    }

    @Test
    fun formatString() {
        println("format")
        Assertions.assertEquals("Lamp 1", LabelProcessor.format(" Lamp1"))
        Assertions.assertEquals("LAMP9", LabelProcessor.format(" LAMP9"))
        Assertions.assertEquals("My Lamp 1", LabelProcessor.format("MyLamp1"))
        Assertions.assertEquals("XXX 1", LabelProcessor.format("XXX 1"))
        Assertions.assertEquals("lamp 1", LabelProcessor.format("lamp1"))
        Assertions.assertEquals("lamp", LabelProcessor.format("lamp"))
        Assertions.assertEquals("Living Ceiling Lamp", LabelProcessor.format("LivingCeilingLamp "))
        Assertions.assertEquals("Living Ceiling Lamp", LabelProcessor.format("LivingCeilingLamp"))
        Assertions.assertEquals("Living Cei Ling Lamp", LabelProcessor.format("Living   CeiLing      Lamp"))
    }

    @Test
    fun `test format removes language with empty label`() {
        val deDog = "HUND"
        val enDog = "DOG"

        val deLabel = Label.MapFieldEntry.newBuilder().setKey("de").addValue(deDog).build()
        val enLabel = Label.MapFieldEntry.newBuilder().setKey("en").addValue(enDog).build()
        val frEmptyLabel = Label.MapFieldEntry.newBuilder().setKey("fr").build()
        val labels = listOf(deLabel, enLabel, frEmptyLabel)

        val formattedLabel = LabelProcessor.format(Label.newBuilder().addAllEntry(labels))

        Assertions.assertEquals(deDog, LabelProcessor.getBestMatch(Locale.GERMAN, formattedLabel))
        Assertions.assertEquals(enDog, LabelProcessor.getBestMatch(Locale.ENGLISH, formattedLabel))
        Assertions.assertEquals(enDog, LabelProcessor.getBestMatch(Locale.FRENCH, formattedLabel))
        Assertions.assertNull(formattedLabel.entryList.firstOrNull() { it.key == "fr" })
    }

    @Test
    fun bestMatch() {
        val builder = Label.newBuilder()
        LabelProcessor.addLabel(builder, Locale.GERMAN, DE)
        LabelProcessor.addLabel(builder, Locale.ENGLISH, EN)
        LabelProcessor.addLabel(builder, LANGUAGE_CODE_TECHNICAL, TECH)
        val label = builder.build()

        Assertions.assertEquals(EN, LabelProcessor.getBestMatch(Locale.ENGLISH, label))
        Assertions.assertEquals(DE, LabelProcessor.getBestMatch(Locale.GERMAN, label))
        Assertions.assertEquals(TECH, LabelProcessor.getBestMatch(LANGUAGE_CODE_TECHNICAL, label))
        Assertions.assertEquals(EN, LabelProcessor.getBestMatch(Locale.FRENCH, label))

    }

    @Test
    fun bestMatchShouldAvoidTechnical() {
        val builder = Label.newBuilder()
        LabelProcessor.addLabel(builder, LANGUAGE_CODE_TECHNICAL, TECH)
        LabelProcessor.addLabel(builder, Locale.GERMAN, DE)
        val label = builder.build()

        Assertions.assertEquals(DE, LabelProcessor.getBestMatch(Locale.ENGLISH, label))
    }
}
