package org.openbase.jul.extension.type.processing;

/*
 * #%L
 * JUL Extension Type Processing
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
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.type.communication.ScopeType;
import org.openbase.type.communication.ScopeType.Scope;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class ScopeProcessorTest {

    private final List<String> components;
    private final String scopeStringRep;

    public ScopeProcessorTest() {
        this.components = new ArrayList<>();
        this.components.add("home");
        this.components.add("kitchen");
        this.components.add(""); // test if empty component is handled correctly
        this.components.add("table");
        this.scopeStringRep = "/home/kitchen/table";
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of generateStringRep method, of class ScopeProcessor.
     */
    @Test
    public void testGenerateStringRep_ScopeTypeScope() throws CouldNotPerformException {
        System.out.println("generateStringRep");
        ScopeType.Scope scope = ScopeType.Scope.newBuilder().addAllComponent(components).build();
        String expResult = scopeStringRep;
        String result = ScopeProcessor.generateStringRep(scope);
        assertEquals(expResult, result);
    }

    /**
     * Test of generateStringRep method, of class ScopeProcessor.
     */
    @Test
    public void testGenerateStringRep_Collection() throws CouldNotPerformException {
        System.out.println("generateStringRep");
        String expResult = scopeStringRep;
        String result = ScopeProcessor.generateStringRep(components);
        assertEquals(expResult, result);
    }

    @Test
    public void testGenerateScope() throws CouldNotPerformException {
        System.out.println("testGenerateScope");
        ScopeType.Scope expected = ScopeType.Scope.newBuilder().addComponent("paradise").addComponent("room").addComponent("device").addComponent("test").build();
        ScopeType.Scope result = ScopeProcessor.generateScope("/paradise/room/device/test");
        assertEquals(expected, result, "Scope not fully generated!");
    }

    @Test
    public void testScopeTransformationChain() throws CouldNotPerformException {
        System.out.println("testGenerateScope");

        ScopeType.Scope expected = ScopeType.Scope.newBuilder().addComponent("paradise").addComponent("room").addComponent("device").addComponent("test").build();
        ScopeType.Scope result_1 = ScopeProcessor.generateScope(ScopeProcessor.generateStringRep(expected));
        assertEquals(expected, result_1, "Scope not fully generated!");
        String result_2 = ScopeProcessor.generateStringRep(result_1);
        assertEquals("/paradise/room/device/test", result_2, "Scope not fully generated!");
    }

    @Test
    public void testConvertIntoValidScopeComponent() {
        assertEquals( "qijijs", ScopeProcessor.convertIntoValidScopeComponent("qijijs"));
        assertEquals("qijijs", ScopeProcessor.convertIntoValidScopeComponent("qi__jijs"));
        assertEquals("qijijs", ScopeProcessor.convertIntoValidScopeComponent("qi_____jijs"));
        assertEquals("quejsss", ScopeProcessor.convertIntoValidScopeComponent("qüjßs"));
        assertEquals("mycomponent", ScopeProcessor.convertIntoValidScopeComponent("_myComponent__"));
        assertEquals("mysweet", ScopeProcessor.convertIntoValidScopeComponent("/my/sweet❤️"));
    }

    @Test
    public void testScopeConcatenation() {
        final Scope scopeA = ScopeProcessor.generateScope("/my/first/scope");
        final Scope scopeB = ScopeProcessor.generateScope("/has/a/suffix");
        final Scope expectedResult = ScopeProcessor.generateScope("/my/first/scope/has/a/suffix");
        final Scope result = ScopeProcessor.concat(scopeA, scopeB);
        assertEquals(expectedResult, result, "Concatenation not valid!");
    }
}
