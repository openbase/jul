/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rst.storage.registry.consistency;

import com.google.protobuf.GeneratedMessage;
import java.util.ArrayList;
import java.util.List;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.processing.StringProcessor;
import org.dc.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.dc.jul.storage.registry.EntryModification;
import org.dc.jul.storage.registry.ProtoBufRegistryInterface;
import rst.spatial.LocationConfigType;
import rst.spatial.PlacementConfigType.PlacementConfig;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractTransformationFrameConsistencyHandler<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractProtoBufRegistryConsistencyHandler<KEY, M, MB> {

    private final List<String> labelCollisionList;
    private final ProtoBufRegistryInterface<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> locationRegistry;

    public AbstractTransformationFrameConsistencyHandler(final ProtoBufRegistryInterface<String, LocationConfigType.LocationConfig, LocationConfigType.LocationConfig.Builder> locationRegistry) {
        this.labelCollisionList = new ArrayList<>();
        this.locationRegistry = locationRegistry;
    }

    /**
     * Methods verifies and updates the transformation frame id for the given placement configuration.
     * If the given placement configuration is up to date this the method returns null.
     *
     * @param label
     * @param placementConfig
     * @return
     * @throws CouldNotPerformException
     * @throws EntryModification
     */
    protected PlacementConfig verifyAndUpdatePlacement(final String label, final PlacementConfig placementConfig) throws CouldNotPerformException, EntryModification {
        try {
            if (label == null || label.isEmpty()) {
                throw new NotAvailableException("label");
            }

            if (placementConfig == null) {
                throw new NotAvailableException("placementconfig");
            }

            String frameId = generateFrameId(label, placementConfig);

            // verify and update frame id
            if (placementConfig.getTransformationFrameId().equals(frameId)) {
                return null;

            }
            return placementConfig.toBuilder().setTransformationFrameId(frameId).build();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not verify and update placement!", ex);
        }
    }

    protected String generateFrameId(final String label, final PlacementConfig placementConfig) throws CouldNotPerformException {
        try {
            String frameId = StringProcessor.transformToIdString(label);

            if (labelCollisionList.contains(frameId.toLowerCase())) {
                return locationRegistry.get(placementConfig.getLocationId()).getMessage().getPlacementConfig().getTransformationFrameId() + "_" + frameId;
            }
            labelCollisionList.add(frameId.toLowerCase());
            return frameId;
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not generate frame id!", ex);
        }
    }

    @Override
    public void reset() {
        labelCollisionList.clear();
    }
}