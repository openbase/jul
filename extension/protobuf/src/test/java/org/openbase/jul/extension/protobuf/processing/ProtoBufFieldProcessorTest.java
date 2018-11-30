package org.openbase.jul.extension.protobuf.processing;

/*-
 * #%L
 * JUL Extension Protobuf
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

import org.junit.*;
import rst.geometry.PoseType.Pose.Builder;
import rst.geometry.RotationType.Rotation;
import rst.spatial.PlacementConfigType.PlacementConfig;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @author <a href="mailto:pLeminoq@openbase.org">Tamino Huxohl</a>
 */
public class ProtoBufFieldProcessorTest {

    public ProtoBufFieldProcessorTest() {
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
     * Test of initFieldWithDefault method, of class ProtoBufFieldProcessor.
     */
    @Test
    public void testInitFieldWithDefault() {
        System.out.println("initFieldWithDefault");

        Rotation.Builder rotation = Rotation.newBuilder().setQx(0.0).setQy(0.0).setQz(0.0);
        assertTrue("Rotation is initialized even though Qw is missing", !rotation.isInitialized());
        ProtoBufFieldProcessor.initFieldWithDefault(rotation, rotation.getInitializationErrorString());

        assertTrue("Rotation is not initialized", rotation.isInitialized());
        assertEquals("Qw has not been initialized with its default value", 1.0, rotation.getQw(), 0.01);
    }

    /**
     * Test of testClearRequiredFields method, of class ProtoBufFieldProcessor.
     */
    @Test
    public void testClearRequiredFields() {
        System.out.println("testClearRequiredFields");

        PlacementConfig.Builder placement = PlacementConfig.newBuilder();
        Builder positionBuilder = placement.getPositionBuilder();
        positionBuilder.getRotationBuilder();
        positionBuilder.getTranslationBuilder();

        assertTrue("Placement is initialized", !placement.isInitialized());
        ProtoBufFieldProcessor.clearRequiredFields(placement);
        assertTrue("Placement is not initialized", placement.isInitialized());
    }
}
