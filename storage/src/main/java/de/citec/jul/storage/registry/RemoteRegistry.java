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
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class RemoteRegistry<KEY, M extends GeneratedMessage, MB extends GeneratedMessage.Builder> extends Registry<KEY, IdentifiableMessage<KEY, M>> {

    public synchronized void notifyRegistryUpdated(final Collection<M> values) throws CouldNotPerformException {
        Map<KEY, IdentifiableMessage<KEY, M>> newRegistryMap = new HashMap<>();
        for (M value : values) {
            IdentifiableMessage<KEY, M> data = new IdentifiableMessage<>(value);
            newRegistryMap.put(data.getId(), data);
        }
        replaceInternalMap(newRegistryMap);
    }

    public M getMessage(final KEY key) throws NotAvailableException {
        return get(key).getMessage();
    }

    public MB getBuilder(final KEY key) throws NotAvailableException {
        return (MB) getMessage(key).toBuilder();
    }

    public M register(final M entry) throws CouldNotPerformException {
        return super.register(new IdentifiableMessage<KEY, M>(entry)).getMessage();
    }

    public M update(final M entry) throws CouldNotPerformException {
        return super.update(new IdentifiableMessage<KEY, M>(entry)).getMessage();
    }

    public M remove(final M entry) throws CouldNotPerformException {
        return super.remove(new IdentifiableMessage<KEY, M>(entry)).getMessage();
    }

    public boolean contrains(final M key) throws CouldNotPerformException {
        return super.contrains(new IdentifiableMessage<KEY, M>(key).getId());
    }
}
