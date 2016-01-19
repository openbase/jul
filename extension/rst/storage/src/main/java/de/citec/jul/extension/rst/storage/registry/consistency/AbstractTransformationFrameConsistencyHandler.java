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
import org.dc.jul.processing.StringProcessor;
import org.dc.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.dc.jul.storage.registry.EntryModification;
import org.dc.jul.storage.registry.ProtoBufRegistryInterface;
import rst.spatial.LocationConfigType;
import rst.spatial.PlacementConfigType;
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
     * @param label
     * @param placementConfig
     * @return
     * @throws CouldNotPerformException
     * @throws EntryModification
     */
    protected PlacementConfig verifyAndUpdatePlacement(final String label, final PlacementConfig placementConfig) throws CouldNotPerformException, EntryModification {
        String frameId = generateFrameId(label, placementConfig);

        // verify and update frame id
        if (placementConfig.getTransformationFrameId().equals(frameId)) {
            return null;

        }
        return PlacementConfigType.PlacementConfig.newBuilder(placementConfig).setTransformationFrameId(frameId).build();
    }

    protected String generateFrameId(final String label, final PlacementConfig placementConfig) throws CouldNotPerformException {
        try {
            String frameId = StringProcessor.transformToIdString(label.toLowerCase());

            if (labelCollisionList.contains(frameId)) {
                return locationRegistry.get(placementConfig.getLocationId()).getMessage().getPlacementConfig().getTransformationFrameId() + "_" + frameId;
            }
            labelCollisionList.add(frameId);
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
