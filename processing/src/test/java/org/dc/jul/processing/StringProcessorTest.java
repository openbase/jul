package org.dc.jul.processing;

/*
 * #%L
 * JUL Processing
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jul.processing.StringProcessor;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class StringProcessorTest {

    public StringProcessorTest() {
    }

    @BeforeClass
    public static void setUpClass() {
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

    @Test
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
    @Test
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
    @Test
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
    @Test
    public void testTransformUpperCaseToCamelCase() {
        System.out.println("transformUpperCaseToCamelCase");
        assertEquals("MyFarm", StringProcessor.transformUpperCaseToCamelCase("My Farm"));
        assertEquals("IAmOnTheWay!", StringProcessor.transformUpperCaseToCamelCase("I_AM_ON_THE_WAY!"));
        assertEquals("HalloMyNameIsNothingToDo.", StringProcessor.transformUpperCaseToCamelCase("Hallo my name is nothing to do."));
        assertEquals("", StringProcessor.transformUpperCaseToCamelCase(""));
        assertEquals("Final", StringProcessor.transformUpperCaseToCamelCase("final"));
    }

    @Test
    public void testTransformToUpperCase() {
        System.out.println("insertSpaceBetweenCamelCase");
        assertEquals("MY_FARM", StringProcessor.transformToUpperCase("MyFarm"));
        assertEquals("I_AM_ON_THE_WAY_TO_HELL_MY_GOD", StringProcessor.transformToUpperCase("I am on the WayToHell my god"));
        assertEquals("HALLO_MY_NAME_IS_NOTHING_TO_DO.", StringProcessor.transformToUpperCase("Hallo my name is nothing to do."));
        assertEquals("I_AM_ON_THE_WAY_TO_HELL_MY_GOD!", StringProcessor.transformToUpperCase("I am on the WayToHell my god!"));
        assertEquals("FINAL", StringProcessor.transformToUpperCase("final"));
    }

    @Test
    public void testfillWithSpaces() {
        assertEquals("MyFarm    ", StringProcessor.fillWithSpaces("MyFarm", 10));
        assertEquals(" 1234 ", StringProcessor.fillWithSpaces(" 1234", 6));
        assertEquals("nospaces", StringProcessor.fillWithSpaces("nospaces", 0));
        assertEquals("   ", StringProcessor.fillWithSpaces("", 3));
    }
}
