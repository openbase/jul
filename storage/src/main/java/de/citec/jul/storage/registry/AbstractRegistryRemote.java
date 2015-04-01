/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.rsb.IdentifiableMessage;
import de.citec.jul.rsb.RSBRemoteService;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <VALUE>
 * @param <M>
 */
public abstract class AbstractRegistryRemote<VALUE extends GeneratedMessage, M extends GeneratedMessage> extends RSBRemoteService<M> {
    
    protected final Registry<String, IdentifiableMessage<VALUE>> remoteRegistry;

    public AbstractRegistryRemote() {
        remoteRegistry = new Registry<>();
    }
    
    protected synchronized void notifyRegistryUpdated(final Collection<VALUE> values) {
        Map<String, IdentifiableMessage<VALUE>> newRegistryMap = new HashMap<>();
        for(VALUE value : values) {
            IdentifiableMessage data = new IdentifiableMessage(value);
            newRegistryMap.put(data.getId(), data);
        }
        remoteRegistry.replaceInternalMap(newRegistryMap);
    }   
}
