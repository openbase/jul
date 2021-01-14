package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
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

import com.google.protobuf.AbstractMessage;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.extension.protobuf.container.ProtoBufMessageMap;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * ConsistencyHandler can be registered at any registry type and will be informed about data changes via the processData Method. 
 * The handler can be used to establish a registry data consistency. 
 * @param <KEY> the registry key type.
 * @param <M>
 * @param <MB>
 */
public interface ProtoBufRegistryConsistencyHandler<KEY extends Comparable<KEY>, M extends AbstractMessage, MB extends M.Builder<MB>, REGISTRY extends ProtoBufRegistry<KEY, M, MB>> extends ConsistencyHandler<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMap<KEY, M, MB>, REGISTRY> {
    
}
