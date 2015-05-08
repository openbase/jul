/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.scope;

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
public class ScopeTransformerTest {

    public ScopeTransformerTest() {
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
     * Test of transform method, of class ScopeTransformer.
     */
    @Test
    public void testTransform_ScopeTypeScope() throws Exception {
        System.out.println("transform");

        List<String> components = new ArrayList<String>();
        components.add("home");
        components.add("kitchen");
        components.add("table");
        ScopeType.Scope scope = ScopeType.Scope.newBuilder().addAllComponent(components).build();
        Scope result = ScopeTransformer.transform(scope);
        assertEquals(ScopeGenerator.generateStringRep(scope), ScopeGenerator.generateStringRep(result));
        assertEquals(ScopeGenerator.generateStringRep(scope), ScopeGenerator.generateStringRep(components));
    }

    /**
     * Test of transform method, of class ScopeTransformer.
     */
    @Test
    public void testTransform_Scope() throws Exception {
        System.out.println("transform");
        List<String> components = new ArrayList<>();
        components.add("home");
        components.add("kitchen");
        components.add("table");
        Scope scope = new Scope(ScopeGenerator.generateStringRep(components));
        ScopeType.Scope result = ScopeTransformer.transform(scope);
        assertEquals(ScopeGenerator.generateStringRep(scope), ScopeGenerator.generateStringRep(result));
        assertEquals(ScopeGenerator.generateStringRep(scope), ScopeGenerator.generateStringRep(components));
    }
}
