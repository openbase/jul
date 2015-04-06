/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import de.citec.jul.processing.FileProcessor;
import de.citec.jul.rsb.IdentifiableMessage;
import de.citec.jul.rsb.ProtobufMessageMap;
import de.citec.jul.storage.file.FileProvider;
import java.io.File;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 * @param <KEY>
 * @param <VALUE>
 * @param <M>
 * @param <MB>
 */
public class ProtoBufFileSynchronizedRegistry<KEY, VALUE extends IdentifiableMessage<KEY, M>, M extends GeneratedMessage, MB extends Message.Builder> extends FileSynchronizedRegistry<KEY, VALUE> {

	private final ProtobufMessageMap<KEY, VALUE, M, MB> regProtobufMessageMap;
	private final Observer<VALUE> observer;

	public ProtoBufFileSynchronizedRegistry(ProtobufMessageMap<KEY, VALUE, M, MB> registry, File databaseDirectory, FileProcessor<VALUE> fileProcessor, FileProvider<Identifiable<KEY>> fileProvider) {
		super(registry, databaseDirectory, fileProcessor, fileProvider);
		this.regProtobufMessageMap = registry;
		this.observer = new Observer<VALUE>() {

			@Override
			public void update(Observable<VALUE> source, VALUE data) throws Exception {
				ProtoBufFileSynchronizedRegistry.this.update(data);
			}
		};
		this.regProtobufMessageMap.addObserver(observer);
	}

	@Override
	public void shutdown() {
		regProtobufMessageMap.removeObserver(observer);
		super.shutdown();
	}
}
