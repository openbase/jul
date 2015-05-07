/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.processing;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
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

}
