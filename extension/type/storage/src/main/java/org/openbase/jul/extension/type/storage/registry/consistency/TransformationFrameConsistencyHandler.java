package org.openbase.jul.extension.type.storage.registry.consistency;

/*
 * #%L
 * JUL Extension Type Storage
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;
import org.openbase.jul.processing.StringProcessor;
import org.openbase.jul.storage.registry.AbstractProtoBufRegistryConsistencyHandler;
import org.openbase.jul.storage.registry.EntryModification;
import org.openbase.jul.storage.registry.ProtoBufRegistry;
import org.openbase.type.domotic.unit.UnitConfigType;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig;
import org.openbase.type.domotic.unit.UnitConfigType.UnitConfig.Builder;
import org.openbase.type.spatial.PlacementConfigType.PlacementConfig;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TransformationFrameConsistencyHandler extends AbstractProtoBufRegistryConsistencyHandler<String, UnitConfigType.UnitConfig, UnitConfig.Builder> {

    private final List<String> labelCollisionList;
    private final ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> locationRegistry;

    public TransformationFrameConsistencyHandler(final ProtoBufRegistry<String, UnitConfig, UnitConfig.Builder> locationRegistry) {
        this.labelCollisionList = new ArrayList<>();
        this.locationRegistry = locationRegistry;
    }

    /**
     * Methods verifies and updates the transformation frame id for the given placement configuration.
     * If the given placement configuration is up to date this the method returns null.
     *
     * @param alias
     * @param placementConfig
     * @return
     * @throws CouldNotPerformException
     * @throws EntryModification
     */
    protected PlacementConfig verifyAndUpdatePlacement(final String alias, final PlacementConfig placementConfig) throws CouldNotPerformException, EntryModification {
        try {
            if (alias == null || alias.isEmpty()) {
                throw new NotAvailableException("label");
            }

            if (placementConfig == null) {
                throw new NotAvailableException("placementconfig");
            }

            String frameId = generateFrameId(alias, placementConfig);

            // verify and update frame id
            if (placementConfig.getTransformationFrameId().equals(frameId)) {
                return null;

            }
            return placementConfig.toBuilder().setTransformationFrameId(frameId).build();
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not verify and update placement!", ex);
        }
    }

    protected String generateFrameId(final String alias, final PlacementConfig placementConfig) throws CouldNotPerformException {
        try {
            String frameId = StringProcessor.transformToIdString(alias);

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
    public void processData(String id, IdentifiableMessage<String, UnitConfig, Builder> entry, ProtoBufMessageMap<String, UnitConfig, Builder> entryMap, ProtoBufRegistry<String, UnitConfig, Builder> registry) throws CouldNotPerformException, EntryModification {
        UnitConfig unitConfig = entry.getMessage();

        if (unitConfig.getAliasList().isEmpty()) {
            throw new NotAvailableException("alias");
        }
        PlacementConfig placementConfig = verifyAndUpdatePlacement(unitConfig.getAlias(0), unitConfig.getPlacementConfig());

        if (placementConfig != null) {
            entry.setMessage(unitConfig.toBuilder().setPlacementConfig(placementConfig), this);
            throw new EntryModification(entry, this);
        }
    }

    @Override
    public void reset() {
        labelCollisionList.clear();
    }
}
