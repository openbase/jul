/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.rsb.IdentifiableMessage;
import java.util.Map;

/**
 *
 * @author mpohling
 * ConsistencyHandler can be registered at any registry type and will be informed about data changes via the processData Method. 
 * The handler can be used to establish a registry data consistency. 
 * @param <KEY> the registry key type.
 * @param <M>
 * @param <MB>
 * @param <SIB>
 */
public interface ProtoBufRegistryConsistencyHandler<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder> extends ConsistencyHandler<KEY, IdentifiableMessage<KEY, M>, Map<KEY, IdentifiableMessage<KEY, M>>, ProtoBufRegistryInterface<KEY, M, MB, SIB>> {
    
}
//ProtobufMessageMap<KEY, M, SIB>