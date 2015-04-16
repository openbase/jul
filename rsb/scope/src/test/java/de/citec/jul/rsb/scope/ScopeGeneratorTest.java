/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb.scope;

import java.util.ArrayList;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rsb.Scope;
import rst.rsb.ScopeType;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
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
    @Test
    public void testGenerateStringRep_Scope() {
        System.out.println("generateStringRep");
        Scope scope = new Scope(scopeStringRep);
        String result = ScopeGenerator.generateStringRep(scope);
        assertEquals(scopeStringRep, result);
    }

    /**
     * Test of generateStringRep method, of class ScopeGenerator.
     */
    @Test
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
    @Test
    public void testGenerateStringRep_Collection() {
        System.out.println("generateStringRep");
        String expResult = scopeStringRep;
        String result = ScopeGenerator.generateStringRep(components);
        assertEquals(expResult, result);
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
