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
 * @param <VALUE>
 * @param <M extends GeneratedMessage, MB>
 * @param <MB>
 */
public class ProtobufMessageMap<KEY, VALUE extends IdentifiableMessage<KEY, M>, M extends GeneratedMessage, MB extends Builder> extends HashMap<KEY, VALUE> implements Map<KEY, VALUE> {

	protected final Logger logger = LoggerFactory.getLogger(ProtobufMessageMap.class);

	private final MB builder;
	private final Observer<VALUE> observer;
	private final Observable<VALUE> observable;

	private final Descriptors.FieldDescriptor fieldDescriptor;

	public ProtobufMessageMap(final MB builder, final Descriptors.FieldDescriptor fieldDescriptor) {
		this.builder = builder;
		this.fieldDescriptor = fieldDescriptor;
		this.observable = new Observable<>();
		this.observer = new Observer<VALUE>() {

			@Override
			public void update(Observable<VALUE> source, VALUE data) throws Exception {
				syncBuilder();
				observable.notifyObservers(source, data);
			}
		};
	}

	@Override
	public VALUE put(KEY key, VALUE value) {
		if (value == null) {
			logger.error("Could not add value!", new NotAvailableException("value"));
			return value;
		}
		VALUE oldValue = super.put(key, value);
		if (oldValue != null) {
			oldValue.removeObserver(observer);
		}
		value.addObserver(observer);
		syncBuilder();
		return oldValue;
	}

	@Override
	public VALUE remove(Object key) {
		VALUE removedValue = super.remove(key);
		if (removedValue != null) {
			removedValue.removeObserver(observer);
			syncBuilder();
		}
		return removedValue;
	}

	@Override
	public void putAll(Map<? extends KEY, ? extends VALUE> valueMap) {
		for (Entry<? extends KEY, ? extends VALUE> entry : valueMap.entrySet()) {
			if (entry.getValue() == null) {
				continue;
			}
			VALUE oldValue = super.put(entry.getKey(), entry.getValue());
			if (oldValue != null) {
				oldValue.removeObserver(observer);
			}
			entry.getValue().addObserver(observer);
		}
		syncBuilder();
	}

	@Override
	public void clear() {
		for (VALUE value : values()) {
			value.removeObserver(observer);
		}
		super.clear();
		syncBuilder();
	}

	private void syncBuilder() {
		synchronized (builder) {
			builder.clearField(fieldDescriptor);
			for (VALUE value : values()) {
				builder.addRepeatedField(fieldDescriptor, value.getMessage());
			}
		}
	}

	public void addObserver(Observer<VALUE> observer) {
		observable.addObserver(observer);
	}

	public void removeObserver(Observer<VALUE> observer) {
		observable.removeObserver(observer);
	}

	public void shutdown() {
		observable.shutdown();
	}
}
