/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf.container;

import de.citec.jul.extension.protobuf.IdentifiableMessage;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.extension.protobuf.BuilderSyncSetup;
import de.citec.jul.extension.protobuf.IdGenerator;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @param <SIB> synchronized internal builder

 */
public class ProtoBufMessageMap<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends Builder<SIB>> extends HashMap<KEY, IdentifiableMessage<KEY, M, MB>> implements ProtoBufMessageMapInterface<KEY, M, MB> {

    protected final Logger logger = LoggerFactory.getLogger(ProtoBufMessageMap.class);

    private boolean shudownDetected = false;
    private final BuilderSyncSetup<SIB> builderSetup;
    private final Observer<IdentifiableMessage<KEY, M, MB>> observer;
    private final Observable<IdentifiableMessage<KEY, M, MB>> observable;


    private final Descriptors.FieldDescriptor fieldDescriptor;

    public ProtoBufMessageMap(final BuilderSyncSetup<SIB> builderSetup, final Descriptors.FieldDescriptor fieldDescriptor) {
        this.builderSetup = builderSetup;
        this.fieldDescriptor = fieldDescriptor;
        this.observable = new Observable<>();
        this.observer = new Observer<IdentifiableMessage<KEY, M, MB>>() {

            @Override
            public void update(Observable<IdentifiableMessage<KEY, M, MB>> source, IdentifiableMessage<KEY, M, MB> data) throws Exception {
                syncBuilder();
                observable.notifyObservers(source, data);
            }
        };
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> put(KEY key, IdentifiableMessage<KEY, M, MB> value) {
        if (value == null) {
            logger.error("Could not add value!", new NotAvailableException("value"));
            return value;
        }
        IdentifiableMessage<KEY, M, MB> oldValue = super.put(key, value);
        if (oldValue != null) {
            oldValue.removeObserver(observer);
        }
        value.addObserver(observer);
        syncBuilder();
        return oldValue;
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> put(IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException {
        return put(value.getId(), value);
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> remove(Object key) {
        IdentifiableMessage<KEY, M, MB> removedValue = super.remove(key);
        if (removedValue != null) {
            removedValue.removeObserver(observer);
            syncBuilder();
        }
        return removedValue;
    }

    @Override
    public void putAll(Map<? extends KEY, ? extends IdentifiableMessage<KEY, M, MB>> valueMap) {
        for (Entry<? extends KEY, ? extends IdentifiableMessage<KEY, M, MB>> entry : valueMap.entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            IdentifiableMessage<KEY, M, MB> oldValue = super.put(entry.getKey(), entry.getValue());
            if (oldValue != null) {
                oldValue.removeObserver(observer);
            }
            entry.getValue().addObserver(observer);
        }
        syncBuilder();
    }

    @Override
    public void clear() {
        for (IdentifiableMessage<KEY, M, MB> value : values()) {
            value.removeObserver(observer);
        }
        super.clear();
        syncBuilder();
    }

    private void syncBuilder() {
        if(shudownDetected) {
            return;
        }
        synchronized (builderSetup) {
            try {
                builderSetup.lockWrite(this);
                SIB builder = builderSetup.getBuilder();
                builder.clearField(fieldDescriptor);
                for (IdentifiableMessage<KEY, M, MB> value : values()) {
                    builder.addRepeatedField(fieldDescriptor, value.getMessage());
                }
            } finally {
                builderSetup.unlockWrite();
            }
        }
    }

    public void addObserver(Observer<IdentifiableMessage<KEY, M, MB>> observer) {
        observable.addObserver(observer);
    }

    public void removeObserver(Observer<IdentifiableMessage<KEY, M, MB>> observer) {
        observable.removeObserver(observer);
    }

    @Override
    public M getMessage(final KEY key) throws CouldNotPerformException {
        return get(key).getMessage();
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> get(final KEY key) throws CouldNotPerformException {
        if (key == null) {
            throw new NotAvailableException("key");
        }

        if (!containsKey(key)) {
            throw new NotAvailableException("Value for key[" + key + "]");
        }
        return super.get(key);
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> get(final M message, final IdGenerator<KEY, M> idGenerator) throws CouldNotPerformException {
        return get(new IdentifiableMessage<KEY, M, MB>(message, idGenerator));
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> get(final IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException {
        return get(value.getId());
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        List<M> messageList = new ArrayList<>();
        for (IdentifiableMessage<KEY, M, MB> messageContainer : values()) {
            messageList.add(messageContainer.getMessage());
        }
        return messageList;
    }

    public void shutdown() {
        observable.shutdown();
        shudownDetected = true;
    }
}
