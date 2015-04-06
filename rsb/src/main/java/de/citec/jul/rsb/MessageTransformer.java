/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.rsb.processing.ProtoBufFileProcessor;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public class MessageTransformer<M extends GeneratedMessage, MB extends M.Builder> implements ProtoBufFileProcessor.TypeToMessageTransformer<IdentifiableMessage<?, M>, M, MB> {

    private final Class<M> messageClass;
    
    public MessageTransformer(Class<M> messageClass) {
        this.messageClass = messageClass;
    }
    
    @Override
    public M transform(IdentifiableMessage<?, M> type) {
        return type.getMessage();
    }

    @Override
    public MB newBuilderForType() throws CouldNotPerformException {
        try {
            Object invoke = messageClass.getMethod("newBuilder").invoke(null);
            return (MB) invoke;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Coult not generate builder out of message class!", ex);
        }
    }

    @Override
    public IdentifiableMessage<?, M> transform(M message) {
        return new IdentifiableMessage<>(message);
    }
}
