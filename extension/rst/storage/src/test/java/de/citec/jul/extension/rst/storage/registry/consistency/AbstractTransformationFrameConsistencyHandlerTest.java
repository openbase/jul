/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rst.storage.registry.consistency;

/*
 * #%L
 * JUL Extension RST Storage
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

import java.util.List;
import java.util.Map;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.extension.protobuf.IdGenerator;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.storage.registry.EntryModification;
import org.dc.jul.storage.registry.ProtoBufRegistryInterface;
import org.dc.jul.storage.registry.Registry;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import rst.spatial.LocationConfigType;
import rst.spatial.LocationConfigType.LocationConfig;
import rst.spatial.PlacementConfigType;
import rst.spatial.PlacementConfigType.PlacementConfig;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class AbstractTransformationFrameConsistencyHandlerTest {

    public static final String DEVICE_A = "DeviceA";
    public static final String DEVICE_B = "DeviceB";
    public static final String DEVICE_AE = "DeviceAE";
    public static final String LOCATION_A = "LocationA";
    public static final String LOCATION_B = "LocationB";
    public static final String LOCATION_AE = "LocationÄ";

    private static final LocationConfig locationA = LocationConfig.newBuilder()
            .setId(LOCATION_A)
            .setPlacementConfig(PlacementConfigType.PlacementConfig.newBuilder()
                    .setLocationId(LOCATION_A)
                    .setTransformationFrameId(LOCATION_A)
                    .build())
            .setRoot(true)
            .build();

    private static final LocationConfig locationB = LocationConfig.newBuilder()
            .setId(LOCATION_B)
            .setPlacementConfig(PlacementConfigType.PlacementConfig.newBuilder()
                    .setLocationId(LOCATION_A)
                    .setTransformationFrameId(LOCATION_B)
                    .build())
            .setRoot(false)
            .build();

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
     * Test of verifyAndUpdatePlacement method, of class AbstractTransformationFrameConsistencyHandler.
     */
    @Test
    public void testVerifyAndUpdatePlacement() throws Exception {
        System.out.println("verifyAndUpdatePlacement");
        PlacementConfig.Builder placementConfigA = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId(LOCATION_A);
        PlacementConfig.Builder placementConfigB = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId(LOCATION_B);
        AbstractTransformationFrameConsistencyHandler instance = new AbstractTransformationFrameConsistencyHandlerImpl();
        assertEquals(DEVICE_A, placementConfigA.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_A, placementConfigA.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(DEVICE_B, placementConfigB.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_B, placementConfigB.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(LOCATION_A + "_" + DEVICE_A, placementConfigA.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_A, placementConfigA.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(LOCATION_B + "_" + DEVICE_B, placementConfigB.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_B, placementConfigB.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(null, instance.verifyAndUpdatePlacement(DEVICE_A, placementConfigA.build()));
        assertEquals(null, instance.verifyAndUpdatePlacement(DEVICE_B, placementConfigB.build()));
    }

    /**
     * Test of generateFrameId method, of class AbstractTransformationFrameConsistencyHandler.
     */
    @Test
    public void testGenerateFrameId() throws Exception {
        System.out.println("generateFrameId");
        PlacementConfig placementConfigA = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId(LOCATION_A)
                .build();
        PlacementConfig placementConfigB = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId(LOCATION_B)
                .build();
        AbstractTransformationFrameConsistencyHandler instance = new AbstractTransformationFrameConsistencyHandlerImpl();
        assertEquals(DEVICE_A, instance.generateFrameId(DEVICE_A, placementConfigA));
        assertEquals(DEVICE_B, instance.generateFrameId(DEVICE_B, placementConfigA));
        assertEquals(LOCATION_B + "_" + DEVICE_B, instance.generateFrameId(DEVICE_B, placementConfigB));
        assertEquals(LOCATION_A + "_" + DEVICE_B, instance.generateFrameId(DEVICE_B, placementConfigA));

    }

    public class AbstractTransformationFrameConsistencyHandlerImpl extends AbstractTransformationFrameConsistencyHandler {

        public AbstractTransformationFrameConsistencyHandlerImpl() {
            super(new ProtoBufRegistryInterface<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder>() {

                @Override
                public void checkAccess() throws RejectedException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public LocationConfigType.LocationConfig register(LocationConfigType.LocationConfig entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean contains(LocationConfigType.LocationConfig key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public LocationConfigType.LocationConfig update(LocationConfigType.LocationConfig entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public LocationConfigType.LocationConfig remove(LocationConfigType.LocationConfig entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public LocationConfigType.LocationConfig getMessage(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public List<LocationConfigType.LocationConfig> getMessages() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public LocationConfigType.LocationConfig.Builder getBuilder(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IdGenerator<String, LocationConfigType.LocationConfig> getIdGenerator() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void loadRegistry() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void saveRegistry() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public Integer getDBVersion() throws NotAvailableException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public String getName() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> register(IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> update(IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> remove(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> remove(IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public IdentifiableMessage<String, LocationConfig, LocationConfigType.LocationConfig.Builder> get(String key) throws CouldNotPerformException {
                    if (locationA.getId().equals(key)) {
                        return new IdentifiableMessage<>(locationA);
                    }

                    if (locationB.getId().equals(key)) {
                        return new IdentifiableMessage<>(locationB);
                    }

                    throw new NotAvailableException(LocationConfig.class);
                }

                @Override
                public List<IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder>> getEntries() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean contains(IdentifiableMessage<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean contains(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public void clear() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public int size() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean isReadOnly() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

                @Override
                public boolean isConsistent() {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }

            });
        }

        @Override
        public void processData(Object id, Identifiable entry, Map entryMap, Registry registry) throws CouldNotPerformException, EntryModification {
            throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
        }
    }
}
