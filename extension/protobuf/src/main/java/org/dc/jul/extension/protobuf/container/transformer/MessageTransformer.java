/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.protobuf.container.transformer;

import org.dc.jul.extension.protobuf.IdentifiableMessage;
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.CouldNotTransformException;
import org.dc.jul.extension.protobuf.IdGenerator;
import org.dc.jul.extension.protobuf.container.MessageContainer;
import org.dc.jul.extension.protobuf.processing.ProtoBufFileProcessor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public abstract class MessageTransformer<T extends MessageContainer<M>, M extends GeneratedMessage, MB extends M.Builder<MB>> implements ProtoBufFileProcessor.TypeToMessageTransformer<T, M, MB> {

    private final Class<M> messageClass;
    
    public MessageTransformer(final Class<M> messageClass) {
        this.messageClass = messageClass;
    }
    
    @Override
    public M transform(final T type) {
        return type.getMessage();
    }

    @Override
    public MB newBuilderForType() throws CouldNotPerformException {
        try {
            Object invoke = messageClass.getMethod("newBuilder").invoke(null);
            return (MB) invoke;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException ex) {
            throw new CouldNotPerformException("Coult not generate builder out of message class!", ex);
        }
    }

    public Class<M> getMessageClass() {
        return messageClass;
    }
}
