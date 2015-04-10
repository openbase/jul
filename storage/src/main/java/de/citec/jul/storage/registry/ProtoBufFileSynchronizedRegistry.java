/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import de.citec.jul.rsb.IdGenerator;
import de.citec.jul.rsb.IdentifiableMessage;
import de.citec.jul.rsb.MessageTransformer;
import de.citec.jul.rsb.ProtobufMessageMap;
import de.citec.jul.rsb.processing.ProtoBufFileProcessor;
import de.citec.jul.storage.file.FileProvider;
import java.io.File;
import java.util.Map;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 * @param <KEY>
 * @param <M> Message
 * @param <MB> Message Builder
 * @param <SIB> Synchronized internal builder
 */
//public class ProtoBufFileSynchronizedRegistry<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder> extends FileSynchronizedRegistry<KEY, IdentifiableMessage<KEY, M>, ProtobufMessageMap<KEY, M, SIB>, ProtoBufRegistryInterface<KEY, M, MB, SIB>> implements ProtoBufRegistryInterface<KEY, M, MB, SIB> {
public class ProtoBufFileSynchronizedRegistry<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder> extends FileSynchronizedRegistry<KEY, IdentifiableMessage<KEY, M>,  Map<KEY, IdentifiableMessage<KEY, M>> , ProtoBufRegistryInterface<KEY, M, MB, SIB>> implements ProtoBufRegistryInterface<KEY, M, MB, SIB> {

    private final ProtobufMessageMap<KEY, M, SIB> protobufMessageMap;
    private final IdGenerator<KEY, M> idGenerator;
	private final Observer<IdentifiableMessage<KEY, M>> observer;

	public ProtoBufFileSynchronizedRegistry(final Class<M> messageClass, final SIB builder, final Descriptors.FieldDescriptor fieldDescriptor, final IdGenerator<KEY, M> idGenerator, final File databaseDirectory, final FileProvider<Identifiable<KEY>> fileProvider) {
        this(messageClass, new ProtobufMessageMap<KEY, M, SIB>(builder, fieldDescriptor), idGenerator, databaseDirectory, fileProvider);
    }
    
	public ProtoBufFileSynchronizedRegistry(final Class<M> messageClass, final ProtobufMessageMap<KEY, M, SIB> internalMap, final IdGenerator<KEY, M> idGenerator, final File databaseDirectory, final FileProvider<Identifiable<KEY>> fileProvider) {
		super(internalMap, databaseDirectory, new ProtoBufFileProcessor<IdentifiableMessage<KEY, M>, M, MB>(new MessageTransformer<M, MB>(messageClass, idGenerator)), fileProvider);
        this.idGenerator = idGenerator;
        this.protobufMessageMap = internalMap;
		this.observer = new Observer<IdentifiableMessage<KEY, M>>() {

			@Override
			public void update(Observable<IdentifiableMessage<KEY, M>> source, IdentifiableMessage<KEY, M> data) throws Exception {
				ProtoBufFileSynchronizedRegistry.this.update(data);
			}
		};
        
		protobufMessageMap.addObserver(observer);
	}

	@Override
	public void shutdown() {
		protobufMessageMap.removeObserver(observer);
		super.shutdown();
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
    public MB getBuilder(KEY key) throws CouldNotPerformException {
        return (MB) getMessage(key).toBuilder();
    }

    @Override
    public IdGenerator<KEY, M> getIdGenerator() {
        return idGenerator;
    }
}