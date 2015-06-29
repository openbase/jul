/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.processing;

import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import java.util.HashMap;
import java.util.logging.Logger;
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
public class VariableProcessorTest {

    private TestVariableProvider provider;

    public VariableProcessorTest() {
        provider = new TestVariableProvider();
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
     * Test of resolveVariables method, of class VariableProcessor.
     */
    @Test
    public void testResolveVariables() throws Exception {
        System.out.println("resolveVariables");
        String context = "${VAR_A} : Hey Mr ${VAR_B} is happy today because of Mrs ${VAR_C}. ${VAR_W}${VAR_O}${VAR_W}";
        boolean throwOnError = true;
        String expResult = "A : Hey Mr B is happy today because of Mrs C. WOW";
        String result = VariableProcessor.resolveVariables(context, throwOnError, provider);
        assertEquals(expResult, result);
    }

    /**
     * Test of resolveVariables method, of class VariableProcessor.
     */
    @Test
    public void testResolveVariablesErrorCase() throws Exception {
        System.out.println("resolveVariables");
        String context = "${VAR_A} : Hey Mr ${VAR_D} is happy today because of Mrs ${VAR_C}. ${VAR_W}${VAR_Y}${VAR_W}";
        boolean throwOnError = true;
        String expResult = "A : Hey Mr  is happy today because of Mrs . WOW";
        try {
            VariableProcessor.resolveVariables(context, throwOnError, provider);
            fail("No exception is thrown in error case!");
        } catch (MultiException ex) {
        }

        String result = VariableProcessor.resolveVariables(context, false, provider);
        assertEquals(expResult, result);
    }

    public class TestVariableProvider implements VariableProvider {

        private HashMap<String, String> varMap = new HashMap<>();

        public TestVariableProvider() {
            varMap.put("VAR_A", "A");
            varMap.put("VAR_B", "B");
            varMap.put("VAR_C", "C");
            varMap.put("VAR_W", "W");
            varMap.put("VAR_O", "O");
        }

        @Override
        public String getName() {
            return "TestVarPro";
        }

        @Override
        public String getValue(String variable) throws NotAvailableException {
            return varMap.get(variable);
        }
    }

}
