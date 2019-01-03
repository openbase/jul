package org.openbase.jul.storage.registry.clone;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import com.google.protobuf.AbstractMessage;

import java.util.HashMap;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMapWrapper;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class ProtoBufCloner<KEY extends Comparable<KEY>, M extends AbstractMessage, MB extends M.Builder<MB>> implements RegistryCloner<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMap<KEY, M, MB>> {

    @Override
    public Map<KEY, IdentifiableMessage<KEY, M, MB>> deepCloneMap(Map<KEY, IdentifiableMessage<KEY, M, MB>> map) throws CouldNotPerformException {
        try {
            HashMap<KEY, IdentifiableMessage<KEY, M, MB>> mapClone = new HashMap<>();

            for (Map.Entry<KEY, IdentifiableMessage<KEY, M, MB>> entry : map.entrySet()) {
                mapClone.put(entry.getKey(), deepCloneEntry(entry.getValue()));
            }
            return mapClone;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not clone registry map!", ex);
        }
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> deepCloneEntry(IdentifiableMessage<KEY, M, MB> entry) throws CouldNotPerformException {
        try {
            return new IdentifiableMessage<>(entry.getMessage());
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not clone Entry!", ex);
        }
    }

    @Override
    public ProtoBufMessageMap<KEY, M, MB> deepCloneRegistryMap(ProtoBufMessageMap<KEY, M, MB> map) throws CouldNotPerformException {
        try {
            ProtoBufMessageMap<KEY, M, MB> mapClone = new ProtoBufMessageMapWrapper<>();

            for (Map.Entry<KEY, IdentifiableMessage<KEY, M, MB>> entry : map.entrySet()) {
                mapClone.put(entry.getKey(), deepCloneEntry(entry.getValue()));
            }
            return mapClone;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not clone registry map!", ex);
        }
    }
}
