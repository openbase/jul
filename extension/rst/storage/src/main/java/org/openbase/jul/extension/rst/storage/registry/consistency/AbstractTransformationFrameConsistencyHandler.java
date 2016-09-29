package org.openbase.jul.extension.rst.storage.registry.consistency;

/*
 * #%L
 * JUL Extension RST Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import com.google.protobuf.GeneratedMessage;
import java.util.ArrayList;
import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import rst.homeautomation.unit.UnitConfigType.UnitConfig;
import rst.spatial.PlacementConfigType.PlacementConfig;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractTransformationFrameConsistencyHandler<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractProtoBufRegistryConsistencyHandler<KEY, M, MB> {

    private final List<String> labelCollisionList;
    private final ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> locationRegistry;

    public AbstractTransformationFrameConsistencyHandler(final ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> locationRegistry) {
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
