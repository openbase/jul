/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rst.storage.registry.consistency;

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

    private static LocationConfig locationA = LocationConfig.newBuilder()
            .setId("locationA")
            .setPlacementConfig(PlacementConfigType.PlacementConfig.newBuilder()
                    .setLocationId("locationA")
                    .setTransformationFrameId("locationa")
                    .build())
            .setRoot(true)
            .build();

    private static LocationConfig locationB = LocationConfig.newBuilder()
            .setId("locationB")
            .setPlacementConfig(PlacementConfigType.PlacementConfig.newBuilder()
                    .setLocationId("locationA")
                    .setTransformationFrameId("locationb")
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
                .setLocationId("locationA");
        PlacementConfig.Builder placementConfigB = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId("locationB");
        AbstractTransformationFrameConsistencyHandler instance = new AbstractTransformationFrameConsistencyHandlerImpl();
        assertEquals("devicea", placementConfigA.setTransformationFrameId(instance.verifyAndUpdatePlacement("DeviceA", placementConfigA.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals("deviceb", placementConfigB.setTransformationFrameId(instance.verifyAndUpdatePlacement("DeviceB", placementConfigB.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals("locationa_devicea", placementConfigA.setTransformationFrameId(instance.verifyAndUpdatePlacement("DeviceA", placementConfigA.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals("locationb_deviceb", placementConfigB.setTransformationFrameId(instance.verifyAndUpdatePlacement("DeviceB", placementConfigB.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(null, instance.verifyAndUpdatePlacement("DeviceA", placementConfigA.build()));
        assertEquals(null, instance.verifyAndUpdatePlacement("DeviceB", placementConfigB.build()));
    }

    /**
     * Test of generateFrameId method, of class AbstractTransformationFrameConsistencyHandler.
     */
    @Test
    public void testGenerateFrameId() throws Exception {
        System.out.println("generateFrameId");
        PlacementConfig placementConfigA = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId("locationA")
                .build();
        PlacementConfig placementConfigB = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId("locationB")
                .build();
        AbstractTransformationFrameConsistencyHandler instance = new AbstractTransformationFrameConsistencyHandlerImpl();
        assertEquals("devicea", instance.generateFrameId("DeviceA", placementConfigA));
        assertEquals("deviceb", instance.generateFrameId("DeviceB", placementConfigA));
        assertEquals("locationb_deviceb", instance.generateFrameId("DeviceB", placementConfigB));
        assertEquals("locationa_deviceb", instance.generateFrameId("DeviceB", placementConfigA));

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
