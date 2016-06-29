package org.openbase.jul.processing;

/*
 * #%L
 * JUL Processing
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

import org.openbase.jul.processing.VariableProvider;
import org.openbase.jul.processing.VariableProcessor;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import java.util.HashMap;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
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
    @Test(timeout = 5000)
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
    @Test(timeout = 5000)
    public void testResolveVariablesErrorCase() throws Exception {
        System.out.println("resolveVariables");
        String context = "${VAR_A} : Hey Mr ${VAR_D} is happy today because of Mrs ${VAR_C}. ${VAR_W}${VAR_Y}${VAR_W}";
        boolean throwOnError = true;
        String expResult = "A : Hey Mr  is happy today because of Mrs C. WW";
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
