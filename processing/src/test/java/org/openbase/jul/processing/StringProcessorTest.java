package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing
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

import org.junit.*;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;

import static org.junit.Assert.assertEquals;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class StringProcessorTest {

    public StringProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    @Test(timeout = 5000)
    public void testFormatHumanReadable() {
        System.out.println("insertSpaceBetweenCamelCase");
        assertEquals("My Farm", StringProcessor.formatHumanReadable("MyFarm"));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.formatHumanReadable("I am on the WayToHell my god!"));
        assertEquals("Hallo my name is nothing to do.", StringProcessor.formatHumanReadable("Hallo my name is nothing to do."));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.formatHumanReadable("I am on the WayToHell my god!"));
        assertEquals("final", StringProcessor.formatHumanReadable("final"));
    }

    /**
     * Test of removeDoubleWhiteSpaces method, of class StringProcessor.
     */
    @Test(timeout = 5000)
    public void testRemoveDoubleWhiteSpaces() {
        System.out.println("insertSpaceBetweenCamelCase");
        assertEquals("My Farm", StringProcessor.removeDoubleWhiteSpaces("My	 		Farm"));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.removeDoubleWhiteSpaces("I   am on the Way To   Hell my	god!"));
        assertEquals("Hallo my name is nothing to do.", StringProcessor.removeDoubleWhiteSpaces("Hallo my name is nothing to do."));
        assertEquals("I am on the Way ToHell my god! ", StringProcessor.removeDoubleWhiteSpaces("I am on the Way ToHell		my god! "));
        assertEquals("final", StringProcessor.removeDoubleWhiteSpaces("final"));
    }

    /**
     * Test of insertSpaceBetweenCamelCase method, of class StringProcessor.
     */
    @Test(timeout = 5000)
    public void testInsertSpaceBetweenCamelCase() {
        System.out.println("insertSpaceBetweenCamelCase");
        assertEquals("My Farm", StringProcessor.insertSpaceBetweenCamelCase("MyFarm"));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.insertSpaceBetweenCamelCase("I am on the WayToHell my god!"));
        assertEquals("Hallo my name is nothing to do.", StringProcessor.insertSpaceBetweenCamelCase("Hallo my name is nothing to do."));
        assertEquals("I am on the Way To Hell my god!", StringProcessor.insertSpaceBetweenCamelCase("I am on the WayToHell my god!"));
        assertEquals("final", StringProcessor.insertSpaceBetweenCamelCase("final"));
    }

    /**
     * Test of transformUpperCaseToCamelCase method, of class StringProcessor.
     */
    @Test(timeout = 5000)
    public void testTransformUpperCaseToCamelCase() {
        System.out.println("transformUpperCaseToCamelCase");
        assertEquals("MyFarm", StringProcessor.transformUpperCaseToCamelCase("My Farm"));
        assertEquals("IAmOnTheWay!", StringProcessor.transformUpperCaseToCamelCase("I_AM_ON_THE_WAY!"));
        assertEquals("HalloMyNameIsNothingToDo.", StringProcessor.transformUpperCaseToCamelCase("Hallo my name is nothing to do."));
        assertEquals("", StringProcessor.transformUpperCaseToCamelCase(""));
        assertEquals("Final", StringProcessor.transformUpperCaseToCamelCase("final"));
    }

    @Test(timeout = 5000)
    public void testTransformToUpperCase() {
        System.out.println("insertSpaceBetweenCamelCase");
        assertEquals("MY_FARM", StringProcessor.transformToUpperCase("MyFarm"));
        assertEquals("I_AM_ON_THE_WAY_TO_HELL_MY_GOD", StringProcessor.transformToUpperCase("I am on the WayToHell my god"));
        assertEquals("HALLO_MY_NAME_IS_NOTHING_TO_DO.", StringProcessor.transformToUpperCase("Hallo my name is nothing to do."));
        assertEquals("I_AM_ON_THE_WAY_TO_HELL_MY_GOD!", StringProcessor.transformToUpperCase("I am on the WayToHell my god!"));
        assertEquals("FINAL", StringProcessor.transformToUpperCase("final"));
    }

    @Test(timeout = 5000)
    public void testTransformToCamelCase() {
        System.out.println("transformToCamelCase");
        assertEquals("MyFarm", StringProcessor.transformToCamelCase("My Farm"));
        assertEquals("IAmOnTheWay!", StringProcessor.transformToCamelCase("I_AM_ON_THE_WAY!"));
        assertEquals("HalloMyNameIsNothingToDo.", StringProcessor.transformToCamelCase("Hallo my name is nothing to do."));
        assertEquals("", StringProcessor.transformToCamelCase(""));
        assertEquals("UndErScore", StringProcessor.transformToCamelCase("-Und-erScore--"));
        assertEquals("Final", StringProcessor.transformToCamelCase("final"));
    }

    @Test(timeout = 5000)
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
