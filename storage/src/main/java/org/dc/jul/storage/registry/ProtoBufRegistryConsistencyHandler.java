/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapInterface;

/**
 *
 * @author mpohling
 * ConsistencyHandler can be registered at any registry type and will be informed about data changes via the processData Method. 
 * The handler can be used to establish a registry data consistency. 
 * @param <KEY> the registry key type.
 * @param <M>
 * @param <MB>
 */
public interface ProtoBufRegistryConsistencyHandler<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends ConsistencyHandler<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMapInterface<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> {
    
}