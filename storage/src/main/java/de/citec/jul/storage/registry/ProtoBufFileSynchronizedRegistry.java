/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.storage.registry.plugin.GitRegistryPlugin;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import de.citec.jps.core.JPService;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.extension.protobuf.BuilderSyncSetup;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import de.citec.jul.extension.protobuf.IdGenerator;
import de.citec.jul.extension.protobuf.IdentifiableMessage;
import de.citec.jul.extension.protobuf.container.ProtoBufMessageMap;
import de.citec.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import de.citec.jul.extension.protobuf.container.ProtoBufMessageMapWrapper;
import de.citec.jul.extension.protobuf.container.transformer.MessageTransformer;
import de.citec.jul.extension.protobuf.processing.ProtoBufFileProcessor;
import de.citec.jul.storage.file.FileProvider;
import de.citec.jul.storage.registry.jp.JPGitRegistryPlugin;
import java.io.File;
import java.util.List;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 * @param <KEY>
 * @param <M> Message
 * @param <MB> Message Builder
 * @param <SIB> Synchronized internal builder
 */
public class ProtoBufFileSynchronizedRegistry<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder<SIB>> extends FileSynchronizedRegistry<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMapInterface<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> implements ProtoBufRegistryInterface<KEY, M, MB> {

    private final ProtoBufMessageMap<KEY, M, MB, SIB> protobufMessageMap;
    private final IdGenerator<KEY, M> idGenerator;
    private final Observer<IdentifiableMessage<KEY, M, MB>> observer;

    public ProtoBufFileSynchronizedRegistry(final Class<M> messageClass, final BuilderSyncSetup<SIB> builderSetup, final Descriptors.FieldDescriptor fieldDescriptor, final IdGenerator<KEY, M> idGenerator, final File databaseDirectory, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException {
        this(messageClass, new ProtoBufMessageMap<KEY, M, MB, SIB>(builderSetup, fieldDescriptor), idGenerator, databaseDirectory, fileProvider);
    }

    public ProtoBufFileSynchronizedRegistry(final Class<M> messageClass, final ProtoBufMessageMap<KEY, M, MB, SIB> internalMap, final IdGenerator<KEY, M> idGenerator, final File databaseDirectory, final FileProvider<Identifiable<KEY>> fileProvider) throws InstantiationException {
        super(internalMap, new ProtoBufMessageMapWrapper<KEY, M, MB, SIB>(internalMap), databaseDirectory, new ProtoBufFileProcessor<IdentifiableMessage<KEY, M, MB>, M, MB>(new MessageTransformer<M, MB>(messageClass, idGenerator)), fileProvider);
        this.idGenerator = idGenerator;
        this.protobufMessageMap = internalMap;
        this.observer = new Observer<IdentifiableMessage<KEY, M, MB>>() {

            @Override
            public void update(Observable<IdentifiableMessage<KEY, M, MB>> source, IdentifiableMessage<KEY, M, MB> data) throws Exception {
                ProtoBufFileSynchronizedRegistry.this.update(data);
            }
        };
        protobufMessageMap.addObserver(observer);
        
        if(JPService.getProperty(JPGitRegistryPlugin.class).getValue()) {
            addPlugin(new GitRegistryPlugin(this));
        }
    }

    @Override
    public void shutdown() {
        protobufMessageMap.removeObserver(observer);
        protobufMessageMap.shutdown();
        super.shutdown();
    }

    @Override
    public M register(final M message) throws CouldNotPerformException {
        return super.register(new IdentifiableMessage<KEY, M, MB>(message, idGenerator)).getMessage();
    }

    @Override
    public boolean contains(final M message) throws CouldNotPerformException {
        return contains(new IdentifiableMessage<>(message, idGenerator).getId());
    }

    @Override
    public M update(final M message) throws CouldNotPerformException {
        return update(new IdentifiableMessage<KEY, M, MB>(message, idGenerator)).getMessage();
    }

    @Override
    public M remove(M locationConfig) throws CouldNotPerformException {
        return remove(new IdentifiableMessage<KEY, M, MB>(locationConfig, idGenerator)).getMessage();
    }

    @Override
    public M getMessage(final KEY id) throws CouldNotPerformException {
        return get(id).getMessage();
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        return protobufMessageMap.getMessages();
    }

    @Override
    public MB getBuilder(KEY key) throws CouldNotPerformException {
        return (MB) getMessage(key).toBuilder();
    }

    @Override
    public IdGenerator<KEY, M> getIdGenerator() {
        return idGenerator;
    }
}
