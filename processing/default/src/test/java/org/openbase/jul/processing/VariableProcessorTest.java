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
import org.junit.jupiter.api.Timeout;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.MultiException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class VariableProcessorTest {

    private TestVariableProvider provider;

    public VariableProcessorTest() {
        provider = new TestVariableProvider();
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of resolveVariables method, of class VariableProcessor.
     */
    @Timeout(5)
    @Test
    public void testResolveVariables() throws Exception {
        System.out.println("testResolveVariables");
        String context = "${VAR_A} : Hey Mr ${VAR_B} is happy today because of Mrs ${VAR_C}. ${VAR_W}${VAR_O}${VAR_W}";
        boolean throwOnError = true;
        String expResult = "A : Hey Mr B is happy today because of Mrs C. WOW";
        String result = VariableProcessor.resolveVariables(context, throwOnError, provider);
        assertEquals(expResult, result);
    }

    /**
     * Test of resolveVariables method, of class VariableProcessor.
     */
    @Timeout(5)
    @Test
    public void testResolveVariablesErrorCase() throws Exception {
        System.out.println("testResolveVariablesErrorCase");
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

    public class TestVariableProvider extends VariableStore {

        public TestVariableProvider() {
            super("TestVarPro");
            store("VAR_A", "A");
            store("VAR_B", "B");
            store("VAR_C", "C");
            store("VAR_W", "W");
            store("VAR_O", "O");
        }
    }
}
