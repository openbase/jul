/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.container.transformer;

import de.citec.jul.extension.rsb.container.IdentifiableMessage;
import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.extension.rsb.processing.ProtoBufFileProcessor;
import de.citec.jul.extension.rsb.util.IdGenerator;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public class MessageTransformer<M extends GeneratedMessage, MB extends M.Builder<MB>> implements ProtoBufFileProcessor.TypeToMessageTransformer<IdentifiableMessage<?, M, MB>, M, MB> {

    private final Class<M> messageClass;
    private final IdGenerator<?, M> idGenerator;
    
    public MessageTransformer(final Class<M> messageClass, final IdGenerator<?, M> idGenerator) {
        this.messageClass = messageClass;
        this.idGenerator = idGenerator;
    }
    
    @Override
    public M transform(final IdentifiableMessage<?, M, MB> type) {
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

    @Override
    public IdentifiableMessage<?, M, MB> transform(final M message) throws CouldNotTransformException {
        try {
            return new IdentifiableMessage<>(message, idGenerator);
        } catch(de.citec.jul.exception.InstantiationException ex) {
            throw new CouldNotTransformException("Given message is invalid!" , ex);
        }
    }
}
