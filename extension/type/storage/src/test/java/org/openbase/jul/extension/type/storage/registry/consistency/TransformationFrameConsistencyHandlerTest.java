package org.openbase.jul.extension.type.storage.registry.consistency;

/*
 * #%L
 * JUL Extension Type Storage
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
import org.openbase.jul.exception.*;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import org.openbase.jul.storage.registry.Registry;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig.Builder;
import org.openbase.type.domotic.unit.location.LocationConfigType.LocationConfig;
import org.openbase.type.spatial.PlacementConfigType;
import org.openbase.type.spatial.PlacementConfigType.PlacementConfig;

import java.io.File;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TransformationFrameConsistencyHandlerTest {

    public static final String DEVICE_A = "DeviceA";
    public static final String DEVICE_B = "DeviceB";
    public static final String DEVICE_AE = "DeviceAE";
    public static final String LOCATION_A = "LocationA";
    public static final String LOCATION_B = "LocationB";
    public static final String LOCATION_AE = "LocationÄ";

    private static final UnitConfig locationA = UnitConfig.newBuilder()
            .setId(LOCATION_A)
            .setPlacementConfig(PlacementConfigType.PlacementConfig.newBuilder()
                    .setLocationId(LOCATION_A)
                    .setTransformationFrameId(LOCATION_A)
                    .build())
            .setLocationConfig(LocationConfig.newBuilder().setRoot(true))
            .build();

    private static final UnitConfig locationB = UnitConfig.newBuilder()
            .setId(LOCATION_B)
            .setPlacementConfig(PlacementConfigType.PlacementConfig.newBuilder()
                    .setLocationId(LOCATION_A)
                    .setTransformationFrameId(LOCATION_B)
                    .build())
            .setLocationConfig(LocationConfig.newBuilder().setRoot(false))
            .build();

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();

    }

    /**
     * Test of verifyAndUpdatePlacement method, of class
     * AbstractTransformationFrameConsistencyHandler.
     */
    @Test
    public void testVerifyAndUpdatePlacement() throws Exception {
        System.out.println("verifyAndUpdatePlacement");
        PlacementConfig.Builder placementConfigA = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId(LOCATION_A);
        PlacementConfig.Builder placementConfigB = PlacementConfigType.PlacementConfig.newBuilder()
                .setLocationId(LOCATION_B);
        TransformationFrameConsistencyHandler instance = new TransformationFrameConsistencyHandlerImpl();
        assertEquals(DEVICE_A, placementConfigA.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_A, placementConfigA.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(DEVICE_B, placementConfigB.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_B, placementConfigB.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(LOCATION_A + "_" + DEVICE_A, placementConfigA.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_A, placementConfigA.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(LOCATION_B + "_" + DEVICE_B, placementConfigB.setTransformationFrameId(instance.verifyAndUpdatePlacement(DEVICE_B, placementConfigB.build()).getTransformationFrameId()).getTransformationFrameId());
        assertEquals(null, instance.verifyAndUpdatePlacement(DEVICE_A, placementConfigA.build()));
        assertEquals(null, instance.verifyAndUpdatePlacement(DEVICE_B, placementConfigB.build()));
    }

    /**
     * Test of generateFrameId method, of class
     * AbstractTransformationFrameConsistencyHandler.
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
        TransformationFrameConsistencyHandler instance = new TransformationFrameConsistencyHandlerImpl();
        assertEquals(DEVICE_A, instance.generateFrameId(DEVICE_A, placementConfigA));
        assertEquals(DEVICE_B, instance.generateFrameId(DEVICE_B, placementConfigA));
        assertEquals(LOCATION_B + "_" + DEVICE_B, instance.generateFrameId(DEVICE_B, placementConfigB));
        assertEquals(LOCATION_A + "_" + DEVICE_B, instance.generateFrameId(DEVICE_B, placementConfigA));

    }

    public class TransformationFrameConsistencyHandlerImpl extends TransformationFrameConsistencyHandler {

        public TransformationFrameConsistencyHandlerImpl() {
            super(new ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder>() {

                @Override
                public void checkWriteAccess() throws RejectedException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void loadRegistry() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void saveRegistry() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Integer getDBVersion() throws NotAvailableException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public File getDatabaseDirectory() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public String getName() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> get(String key) throws CouldNotPerformException {
                    if (locationA.getId().equals(key)) {
                        return new IdentifiableMessage<>(locationA);
                    }

                    if (locationB.getId().equals(key)) {
                        return new IdentifiableMessage<>(locationB);
                    }

                    throw new NotAvailableException(LocationConfig.class);
                }

                @Override
                public boolean contains(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void clear() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public int size() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isReadOnly() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isConsistent() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isSandbox() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isEmpty() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isReady() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isValueAvailable() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void shutdown() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public UnitConfig register(UnitConfig entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean contains(UnitConfig key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public UnitConfig update(UnitConfig entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public UnitConfig remove(UnitConfig entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public UnitConfig getMessage(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<UnitConfig> getMessages() throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public UnitConfig.Builder getBuilder(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> register(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> update(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> remove(String key) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> remove(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<IdentifiableMessage<String, UnitConfig, Builder>> removeAll(Collection<IdentifiableMessage<String, UnitConfig, Builder>> identifiableMessages) throws MultiException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<IdentifiableMessage<String, UnitConfig, Builder>> removeAllByKey(Collection<String> strings) throws MultiException, InvalidStateException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public List<IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> getEntries() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean contains(IdentifiableMessage<String, UnitConfig, UnitConfig.Builder> entry) throws CouldNotPerformException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void addDependencyObserver(Observer<Registry<String, IdentifiableMessage<String, UnitConfig, Builder>>, Map<String, IdentifiableMessage<String, UnitConfig, Builder>>> observer) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void removeDependencyObserver(Observer<Registry<String, IdentifiableMessage<String, UnitConfig, Builder>>, Map<String, IdentifiableMessage<String, UnitConfig, Builder>>> observer) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isShutdownInitiated() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean isLocalRegistry() {
                    return true;
                }

                @Override
                public void addObserver(Observer<DataProvider<Map<String, IdentifiableMessage<String, UnitConfig, Builder>>>, Map<String, IdentifiableMessage<String, UnitConfig, Builder>>> observer) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void removeObserver(Observer<DataProvider<Map<String, IdentifiableMessage<String, UnitConfig, Builder>>>, Map<String, IdentifiableMessage<String, UnitConfig, Builder>>> observer) {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> getValue() throws NotAvailableException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void waitForValue(long timeout, TimeUnit timeUnit) throws InterruptedException, NotAvailableException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean tryLockRegistry() throws RejectedException {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public void unlockRegistry() {
                    // Not supported
                }

                @Override
                public Future<Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>>> getValueFuture() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public Map<String, IdentifiableMessage<String, UnitConfig, UnitConfig.Builder>> getEntryMap() {
                    throw new UnsupportedOperationException("Not supported yet.");
                }

                @Override
                public boolean recursiveTryLockRegistry(Set<Registry> lockedRegistries) throws RejectedException {
                    throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                }
            });
        }
    }
}
