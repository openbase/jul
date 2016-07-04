package org.openbase.jul.extension.rsb.scope;

/*
 * #%L
 * JUL Extension RSB Scope
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
import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.openbase.jul.exception.CouldNotPerformException;
import rsb.Scope;
import rst.rsb.ScopeType;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class ScopeGeneratorTest {

    private final List<String> components;
    private final String scopeStringRep;

    public ScopeGeneratorTest() {
        this.components = new ArrayList<String>();
        this.components.add("home");
        this.components.add("kitchen");
        this.components.add("table");
        this.scopeStringRep = "/home/kitchen/table/";
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
     * Test of generateStringRep method, of class ScopeGenerator.
     */
    @Test(timeout = 5000)
    public void testGenerateStringRep_Scope() {
        System.out.println("generateStringRep");
        Scope scope = new Scope(scopeStringRep);
        String result = ScopeGenerator.generateStringRep(scope);
        assertEquals(scopeStringRep, result);
    }

    /**
     * Test of generateStringRep method, of class ScopeGenerator.
     */
    @Test(timeout = 5000)
    public void testGenerateStringRep_ScopeTypeScope() {
        System.out.println("generateStringRep");
        ScopeType.Scope scope = ScopeType.Scope.newBuilder().addAllComponent(components).build();
        String expResult = scopeStringRep;
        String result = ScopeGenerator.generateStringRep(scope);
        assertEquals(expResult, result);
    }

    /**
     * Test of generateStringRep method, of class ScopeGenerator.
     */
    @Test(timeout = 5000)
    public void testGenerateStringRep_Collection() {
        System.out.println("generateStringRep");
        String expResult = scopeStringRep;
        String result = ScopeGenerator.generateStringRep(components);
        assertEquals(expResult, result);
    }

    @Test(timeout = 5000)
    public void testGenerateScope() throws CouldNotPerformException {
        System.out.println("testGenerateScope");
        ScopeType.Scope expected = ScopeType.Scope.newBuilder().addComponent("paradise").addComponent("room").addComponent("device").addComponent("test").build();
        ScopeType.Scope result = ScopeGenerator.generateScope("/paradise/room/device/test");
        assertEquals("Scope not fully generated!", expected, result);
    }

    @Test(timeout = 5000)
    public void testScopeTransfromationChain() throws CouldNotPerformException {
        System.out.println("testGenerateScope");

        ScopeType.Scope expected = ScopeType.Scope.newBuilder().addComponent("paradise").addComponent("room").addComponent("device").addComponent("test").build();
        ScopeType.Scope result_1 = ScopeGenerator.generateScope(ScopeGenerator.generateStringRep(expected));
        assertEquals("Scope not fully generated!", expected, result_1);
        String result_2 = ScopeGenerator.generateStringRep(result_1);
        assertEquals("Scope not fully generated!", "/paradise/room/device/test/", result_2);
    }

    /**
     * Test of setupLocationScope method, of class ScopeGenerator.
     */
//    @Test
    public void testSetupLocationScope() throws Exception {
//        System.out.println("setupLocationScope");
//        LocationConfigType.LocationConfig locationConfig = null;
//        ProtoBufMessageMapInterface<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> entryMap = new ProtoBufMessageMap<>(DescriptorProtos.SourceCodeInfo.Location.newBuilder(),LocationConfigType.LocationConfig.newBuilder().getDescriptorForType().findFieldByNumber(LocationRegistryType.LocationRegistry.LOCATION_CONFIGS_FIELD_NUMBER));
//        new IdentifiableMessage<String, LocationConfig, LocationConfig.Builder>(locationConfig.newBuilderForType());
//
//        entryMap.put(new LocationConfig.newBuilder());
//        ScopeType.Scope expResult = null;
//        ScopeType.Scope result = ScopeGenerator.setupLocationScope(locationConfig, registry);
//        assertEquals(expResult, result);
    }

    /**
     * Test of setupDeviceScope method, of class ScopeGenerator.
     */
//    @Test
    public void testSetupDeviceScope() throws Exception {
//        System.out.println("setupDeviceScope");
//        DeviceConfigType.DeviceConfig deviceConfig = null;
//        ProtoBufMessageMapInterface<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> registry = null;
//        ScopeType.Scope expResult = null;
//        ScopeType.Scope result = ScopeGenerator.setupDeviceScope(deviceConfig, registry);
//        assertEquals(expResult, result);
    }
}
