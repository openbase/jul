/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.protobuf.IdGenerator;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapWrapper;
import org.dc.jul.storage.registry.clone.ProtoBufCloner;
import java.util.List;
import java.util.Map;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 * @param <KEY>
 * @param <M> Message
 * @param <MB> Message Builder
 * @param <SIB> Synchronized internal builder
 */
public class ProtoBufFileSynchronizedRegistrySandbox<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder<SIB>> extends FileSynchronizedRegistrySandbox<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMapInterface<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> implements ProtoBufRegistryInterface<KEY, M, MB> {

    private final IdGenerator<KEY, M> idGenerator;

    public ProtoBufFileSynchronizedRegistrySandbox(final IdGenerator<KEY, M> idGenerator, final Descriptors.FieldDescriptor fieldDescriptor) throws CouldNotPerformException {
        super(new ProtoBufMessageMapWrapper<>(), new ProtoBufCloner<>(idGenerator));
        this.idGenerator = idGenerator;
    }

    @Override
    public M register(final M message) throws CouldNotPerformException {
        return super.register(new IdentifiableMessage<>(message, idGenerator)).getMessage();
    }

    @Override
    public boolean contains(final M message) throws CouldNotPerformException {
        return contains(new IdentifiableMessage<>(message, idGenerator).getId());
    }

    @Override
    public M update(final M message) throws CouldNotPerformException {
        return update(new IdentifiableMessage<>(message, idGenerator)).getMessage();
    }

    @Override
    public M remove(M locationConfig) throws CouldNotPerformException {
        return remove(new IdentifiableMessage<>(locationConfig, idGenerator)).getMessage();
    }

    @Override
    public M getMessage(final KEY id) throws CouldNotPerformException {
        return get(id).getMessage();
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        return entryMap.getMessages();
    }

    @Override
    public MB getBuilder(KEY key) throws CouldNotPerformException {
        return (MB) getMessage(key).toBuilder();
    }

    @Override
    public IdGenerator<KEY, M> getIdGenerator() {
        return idGenerator;
    }

    @Override
    public void sync(ProtoBufMessageMapInterface<KEY, M, MB> map) throws CouldNotPerformException {
        try {
            entryMap.clear();
            for (Map.Entry<KEY, IdentifiableMessage<KEY, M, MB>> entry : map.entrySet()) {
                IdentifiableMessage<KEY, M, MB> copy = new IdentifiableMessage<>(entry.getValue());
                entryMap.put(copy.getId(), copy);
            }
            consistent = true;
        } catch (Exception ex) {
            throw new CouldNotPerformException("FATAL: Sendbox sync failed!", ex);
        }
    }
}
