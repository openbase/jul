package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing Default
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class StringProcessorTest {

    public StringProcessorTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @Test
    public void testFormatHumanReadable() {
        System.out.println("insertSpaceBetweenPascalCase");
        assertEquals("My Farm", StringProcessor.formatHumanReadable("MyFarm"));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.formatHumanReadable("I am on the WayToHell my god!"));
        assertEquals("Hallo my name is nothing to do.", StringProcessor.formatHumanReadable("Hallo my name is nothing to do."));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.formatHumanReadable("I am on_ the WayToHell my god!"));
        assertEquals("final", StringProcessor.formatHumanReadable("final"));
    }

    /**
     * Test of removeDoubleWhiteSpaces method, of class StringProcessor.
     */
    @Test
    public void testRemoveDoubleWhiteSpaces() {
        System.out.println("insertSpaceBetweenPascalCase");
        assertEquals("My Farm", StringProcessor.removeDoubleWhiteSpaces("My	 		Farm"));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.removeDoubleWhiteSpaces("I   am on the Way To   Hell my	god!"));
        assertEquals("Hallo my name is nothing to do.", StringProcessor.removeDoubleWhiteSpaces("Hallo my name is nothing to do."));
        assertEquals("I am on the Way ToHell my god! ", StringProcessor.removeDoubleWhiteSpaces("I am on the Way ToHell		my god! "));
        assertEquals("final", StringProcessor.removeDoubleWhiteSpaces("final"));
    }

    /**
     * Test of insertSpaceBetweenPascalCase method, of class StringProcessor.
     */
    @Test
    public void testInsertSpaceBetweenPascalcase() {
        System.out.println("insertSpaceBetweenPascalCase");
        assertEquals("My Farm", StringProcessor.insertSpaceBetweenPascalCase("MyFarm"));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.insertSpaceBetweenPascalCase("I am on the WayToHell my god!"));
        assertEquals("Hallo my name is nothing to do.", StringProcessor.insertSpaceBetweenPascalCase("Hallo my name is nothing to do."));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.insertSpaceBetweenPascalCase("I am on the WayToHell my god!"));
        assertEquals("final", StringProcessor.insertSpaceBetweenPascalCase("final"));
        assertEquals("C8I7", StringProcessor.insertSpaceBetweenPascalCase("C8I7"));
        assertEquals("", StringProcessor.insertSpaceBetweenPascalCase(""));
        assertEquals("O", StringProcessor.insertSpaceBetweenPascalCase("O"));
        assertEquals("i", StringProcessor.insertSpaceBetweenPascalCase("i"));
    }

    /**
     * Test of transformUpperCaseToPascalCase method, of class StringProcessor.
     */
    @Test
    public void testTransformUpperCaseToPascalcase() {
        System.out.println("transformUpperCaseToPascalCase");
        assertEquals("MyFarm", StringProcessor.transformUpperCaseToPascalCase("My Farm"));
        assertEquals("IAmOnTheWay!", StringProcessor.transformUpperCaseToPascalCase("I_AM_ON_THE_WAY!"));
        assertEquals("HalloMyNameIsNothingToDo.", StringProcessor.transformUpperCaseToPascalCase("Hallo my name is nothing to do."));
        assertEquals("", StringProcessor.transformUpperCaseToPascalCase(""));
        assertEquals("Final", StringProcessor.transformUpperCaseToPascalCase("final"));
    }

    @Test
    public void testTransformToUpperCase() {
        System.out.println("insertSpaceBetweenPascalCase");
        assertEquals("MY_FARM", StringProcessor.transformToUpperCase("MyFarm"));
        assertEquals("I_AM_ON_THE_WAY_TO_HELL_MY_GOD", StringProcessor.transformToUpperCase("I am on the WayToHell my god"));
        assertEquals("HALLO_MY_NAME_IS_NOTHING_TO_DO.", StringProcessor.transformToUpperCase("Hallo my name is nothing to do."));
        assertEquals("I_AM_ON_THE_WAY_TO_HELL_MY_GOD!", StringProcessor.transformToUpperCase("I am on the WayToHell my god!"));
        assertEquals("FINAL", StringProcessor.transformToUpperCase("final"));
        assertEquals("UND_ER_SCORE", StringProcessor.transformToUpperCase("-Und-erScore--"));
    }

    @Test
    public void testTransformToPascalcase() {
        System.out.println("transformToPascalCase");
        assertEquals("MyFarm", StringProcessor.transformToPascalCase("My Farm"));
        assertEquals("IAmOnTheWay!", StringProcessor.transformToPascalCase("I_AM_ON_THE_WAY!"));
        assertEquals("HalloMyNameIsNothingToDo.", StringProcessor.transformToPascalCase("Hallo my name is nothing to do."));
        assertEquals("", StringProcessor.transformToPascalCase(""));
        assertEquals("UndErScore", StringProcessor.transformToPascalCase("-Und-erScore--"));
        assertEquals("Final", StringProcessor.transformToPascalCase("final"));
    }

    @Test
    public void testTransformToKebabCase() {
        System.out.println("transformToKebabCase");
        assertEquals("my-farm", StringProcessor.transformToKebabCase("My Farm"));
        assertEquals("i-am-on-the-way!", StringProcessor.transformToKebabCase("I_AM_ON_THE_WAY!"));
        assertEquals("hallo-my-name-is-nothing-to-do.", StringProcessor.transformToKebabCase("Hallo my name is nothing to do."));
        assertEquals("", StringProcessor.transformToKebabCase(""));
        assertEquals("und-er-score", StringProcessor.transformToKebabCase("-Und-erScore--"));
        assertEquals("final", StringProcessor.transformToKebabCase("final"));
    }

    @Test
    public void testFillWithSpaces() {
        System.out.println("testFillWithSpaces");
        assertEquals("MyFarm    ", StringProcessor.fillWithSpaces("MyFarm", 10));
        assertEquals(" 1234 ", StringProcessor.fillWithSpaces(" 1234", 6));
        assertEquals("nospaces", StringProcessor.fillWithSpaces("nospaces", 0));
        assertEquals("   ", StringProcessor.fillWithSpaces("", 3));
    }

    @Test
    public void transformToNormalizedFileName() {
        System.out.println("transformToNormalizedFileName");
        assertEquals("/hi/this/is/a/normal/path", StringProcessor.transformToNormalizedFileName("///hi/this//is/a/normal/path/"));
        assertEquals("filename", StringProcessor.transformToNormalizedFileName("filename"));
        assertEquals("/ho", StringProcessor.transformToNormalizedFileName("/ho"));
        assertEquals("this/is", StringProcessor.transformToNormalizedFileName("this///is"));
    }

    @Test
    public void testRemoveWhiteSpaces() {
        System.out.println("testRemoveWhiteSpaces");
        final String value = "testString";
        final String[] whiteSpaces = {" ", "  ", "\t", "\t ", "\n", "\n\t", " \t \n"};
        for (String whiteSpace : whiteSpaces) {
            assertEquals(value, StringProcessor.removeWhiteSpaces(value + whiteSpace));
        }
    }
}
