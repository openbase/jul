/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message.Builder;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M extends GeneratedMessage, MB>
 * @param <SIB> Synchronized internal Builder
 */
public class ProtobufMessageMap<KEY, M extends GeneratedMessage, SIB extends Builder> extends HashMap<KEY, IdentifiableMessage<KEY, M>> implements Map<KEY, IdentifiableMessage<KEY, M>> {

	protected final Logger logger = LoggerFactory.getLogger(ProtobufMessageMap.class);

	private final SIB builder;
	private final Observer<IdentifiableMessage<KEY, M>> observer;
	private final Observable<IdentifiableMessage<KEY, M>> observable;

	private final Descriptors.FieldDescriptor fieldDescriptor;

	public ProtobufMessageMap(final SIB builder, final Descriptors.FieldDescriptor fieldDescriptor) {
		this.builder = builder;
		this.fieldDescriptor = fieldDescriptor;
		this.observable = new Observable<>();
		this.observer = new Observer<IdentifiableMessage<KEY, M>>() {

			@Override
			public void update(Observable<IdentifiableMessage<KEY, M>> source, IdentifiableMessage<KEY, M> data) throws Exception {
				syncBuilder();
				observable.notifyObservers(source, data);
			}
		};
	}

	@Override
	public IdentifiableMessage<KEY, M> put(KEY key, IdentifiableMessage<KEY, M> value) {
		if (value == null) {
			logger.error("Could not add value!", new NotAvailableException("value"));
			return value;
		}
		IdentifiableMessage<KEY, M> oldValue = super.put(key, value);
		if (oldValue != null) {
			oldValue.removeObserver(observer);
		}
		value.addObserver(observer);
		syncBuilder();
		return oldValue;
	}

	@Override
	public IdentifiableMessage<KEY, M> remove(Object key) {
		IdentifiableMessage<KEY, M> removedValue = super.remove(key);
		if (removedValue != null) {
			removedValue.removeObserver(observer);
			syncBuilder();
		}
		return removedValue;
	}

	@Override
	public void putAll(Map<? extends KEY, ? extends IdentifiableMessage<KEY, M>> valueMap) {
		for (Entry<? extends KEY, ? extends IdentifiableMessage<KEY, M>> entry : valueMap.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			IdentifiableMessage<KEY, M> oldValue = super.put(entry.getKey(), entry.getValue());
			if (oldValue != null) {
				oldValue.removeObserver(observer);
			}
			entry.getValue().addObserver(observer);
		}
		syncBuilder();
	}

	@Override
	public void clear() {
		for (IdentifiableMessage<KEY, M> value : values()) {
			value.removeObserver(observer);
		}
		super.clear();
		syncBuilder();
	}

	private void syncBuilder() {
		synchronized (builder) {
			builder.clearField(fieldDescriptor);
			for (IdentifiableMessage<KEY, M> value : values()) {
				builder.addRepeatedField(fieldDescriptor, value.getMessage());
			}
		}
	}

	public void addObserver(Observer<IdentifiableMessage<KEY, M>> observer) {
		observable.addObserver(observer);
	}

	public void removeObserver(Observer<IdentifiableMessage<KEY, M>> observer) {
		observable.removeObserver(observer);
	}

	public void shutdown() {
		observable.shutdown();
	}
}
