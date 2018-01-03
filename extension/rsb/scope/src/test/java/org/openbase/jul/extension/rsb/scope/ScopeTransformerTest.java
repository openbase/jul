package org.openbase.jul.extension.rsb.scope;

/*
 * #%L
 * JUL Extension RSB Scope
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import rsb.Scope;
import rst.rsb.ScopeType;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class ScopeTransformerTest {

    public ScopeTransformerTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
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
    @Test(timeout = 5000)
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
    @Test(timeout = 5000)
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
