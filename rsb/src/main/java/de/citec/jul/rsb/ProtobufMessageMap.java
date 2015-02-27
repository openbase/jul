/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message.Builder;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <BUILDER>
 */
public class ProtobufMessageMap<KEY, VALUE extends IdentifiableMessage, BUILDER extends Builder> extends HashMap<KEY, VALUE> implements Map<KEY, VALUE> {

    private final BUILDER builder;

    private final Descriptors.FieldDescriptor fieldDescriptor;

    public ProtobufMessageMap(final BUILDER builder, final Descriptors.FieldDescriptor fieldDescriptor) {
        this.builder = builder;
        this.fieldDescriptor = fieldDescriptor;
    }

    @Override
    public VALUE put(KEY key, VALUE value) {
        VALUE oldValue = super.put(key, value);
        syncBuilder();
        return oldValue;
    }

    @Override
    public VALUE remove(Object key) {
        VALUE removedValue = super.remove(key);
        syncBuilder();
        return removedValue;
    }

    @Override
    public void putAll(Map<? extends KEY, ? extends VALUE> m) {
        super.putAll(m);
        syncBuilder();
    }

    @Override
    public void clear() {
        super.clear();
        syncBuilder();
    }

    private void syncBuilder() {
        synchronized (builder) {
            builder.clearField(fieldDescriptor);
            for (VALUE value : values()) {
                builder.addRepeatedField(fieldDescriptor, value.getMessageOrBuilder());
            }
        }
    }
}
