/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class ProtobufListDiffTest {
    
    public ProtobufListDiffTest() {
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

    /**
     * Test of getNewMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetNewMessages() {
        System.out.println("getNewMessages");
        ProtobufListDiff instance = null;
        ProtobufListDiff.IdentifiableValueMap expResult = null;
        ProtobufListDiff.IdentifiableValueMap result = instance.getNewMessages();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getUpdatedMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetUpdatedMessages() {
        System.out.println("getUpdatedMessages");
        ProtobufListDiff instance = null;
        ProtobufListDiff.IdentifiableValueMap expResult = null;
        ProtobufListDiff.IdentifiableValueMap result = instance.getUpdatedMessages();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }

    /**
     * Test of getRemovedMessages method, of class ProtobufListDiff.
     */
    @Test
    public void testGetRemovedMessages() {
        System.out.println("getRemovedMessages");
        ProtobufListDiff instance = null;
        ProtobufListDiff.IdentifiableValueMap expResult = null;
        ProtobufListDiff.IdentifiableValueMap result = instance.getRemovedMessages();
        assertEquals(expResult, result);
        // TODO review the generated test code and remove the default call to fail.
        fail("The test case is a prototype.");
    }
    
}
