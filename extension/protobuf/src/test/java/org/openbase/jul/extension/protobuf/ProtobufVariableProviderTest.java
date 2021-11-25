package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import org.openbase.type.domotic.unit.UnitConfigType;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtobufVariableProviderTest {

    public ProtobufVariableProviderTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of getName method, of class ProtobufVariableProvider.
     */
    @Test
    public void testGetName() {

        UnitConfigType.UnitConfig config = UnitConfig.getDefaultInstance();
        ProtobufVariableProvider instance = new ProtobufVariableProvider(config);
        String expResult = "UnitConfig" + ProtobufVariableProvider.NAME_SUFIX;
        String result = instance.getName();
        assertEquals(expResult, result);
    }

    /**
     * Test of getValue method, of class ProtobufVariableProvider.
     */
    @Test
    public void testGetValue() throws Exception {
        UnitConfigType.UnitConfig config = UnitConfig.getDefaultInstance();
        config = config.toBuilder().setUnitHostId("TestHost").build();
        config = config.toBuilder().setId("TestID").build();
        ProtobufVariableProvider instance = new ProtobufVariableProvider(config);

        assertEquals("TestHost", instance.getValue("UNIT_HOST_ID"));
        assertEquals("TestID", instance.getValue("ID"));
    }

}
