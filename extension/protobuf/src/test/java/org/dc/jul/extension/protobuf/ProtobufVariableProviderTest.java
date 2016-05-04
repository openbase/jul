package org.dc.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
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

import org.dc.jul.extension.protobuf.ProtobufVariableProvider;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rst.homeautomation.unit.UnitConfigType;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine
 * Threepwood</a>
 */
public class ProtobufVariableProviderTest {

    public ProtobufVariableProviderTest() {
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
     * Test of getName method, of class ProtobufVariableProvider.
     */
    @Test(timeout = 60000)
    public void testGetName() {

        UnitConfigType.UnitConfig config = UnitConfig.getDefaultInstance();
        System.out.println("getName");
        ProtobufVariableProvider instance = new ProtobufVariableProvider(config);
        String expResult = "UnitConfig" + ProtobufVariableProvider.NAME_SUFIX;
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValue method, of class ProtobufVariableProvider.
     */
    @Test(timeout = 60000)
    public void testGetValue() throws Exception {
        UnitConfigType.UnitConfig config = UnitConfig.getDefaultInstance();
        config = config.toBuilder().setLabel("TestLabel").build();
        config = config.toBuilder().setId("TestID").build();
        System.out.println("getValue");
        ProtobufVariableProvider instance = new ProtobufVariableProvider(config);
        
        assertEquals("TestLabel", instance.getValue("LABEL"));
        assertEquals("TestID", instance.getValue("ID"));
    }

}
