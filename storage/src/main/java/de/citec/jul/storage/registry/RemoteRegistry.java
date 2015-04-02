/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.rsb.IdentifiableMessage;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public class RemoteRegistry<M extends GeneratedMessage, MB extends GeneratedMessage.Builder> extends Registry<String, IdentifiableMessage<M>> {

    public synchronized void notifyRegistryUpdated(final Collection<M> values) throws CouldNotPerformException {
        Map<String, IdentifiableMessage<M>> newRegistryMap = new HashMap<>();
        for (M value : values) {
            IdentifiableMessage<M> data = new IdentifiableMessage(value);
            newRegistryMap.put(data.getId(), data);
        }
        replaceInternalMap(newRegistryMap);
    }

    public M getMessage(final String key) throws NotAvailableException {
        return get(key).getMessage();
    }

    public MB getBuilder(final String key) throws NotAvailableException {
        return (MB) getMessage(key).toBuilder();
    }

    public M register(final M entry) throws CouldNotPerformException {
        return register(new IdentifiableMessage<>(entry)).getMessage();
    }

    public M update(final M entry) throws CouldNotPerformException {
        return update(new IdentifiableMessage<>(entry)).getMessage();
    }

    public M remove(final M entry) throws CouldNotPerformException {
        return remove(new IdentifiableMessage<>(entry)).getMessage();
    }

    public boolean contrains(final M key) throws CouldNotPerformException {
        return contrains(new IdentifiableMessage<>(key).getId());
    }
}
