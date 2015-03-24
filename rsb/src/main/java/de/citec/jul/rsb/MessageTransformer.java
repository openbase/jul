/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.rsb.processing.ProtoBufFileProcessor;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public class MessageTransformer<M extends GeneratedMessage, MB extends M.Builder> implements ProtoBufFileProcessor.TypeToMessageTransformer<IdentifiableMessage<M>, M, MB> {

    private Class<M> messageClass;
    
    public MessageTransformer(Class<M> messageClass) {
        this.messageClass = messageClass;
    }

    
    
    
    @Override
    public M transform(IdentifiableMessage<M> type) {
        return type.getMessageOrBuilder();
    }

    @Override
    public MB newBuilderForType() throws CouldNotPerformException {
        try {
//            Class<M> detectMessageClass = detectMessageClass();
            Object invoke = messageClass.getMethod("newBuilder").invoke(null);
            return (MB) invoke;
//                Class<M> messageClass = detectMessageClass();
//                Class<MB> builder;
//                Message
//                builder 
//                messageClass
//                return (MB) .newInstance().newBuilderForType();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Coult not generate builder out of message class!", ex);
        }
    }

    @Override
    public IdentifiableMessage<M> transform(M message) {
        return new IdentifiableMessage<>(message);
    }
    
//    public Class<M> detectMessageClass() {
//        Type[] genericInterfaces = getClass().getGenericInterfaces();
//        ParameterizedType p = ((ParameterizedType)genericInterfaces[0]);
//        return (Class<M>) (p.getActualTypeArguments()[1]);
//        
////        Type genericSuperclass = getClass().getGenericSuperclass();
////        String typeName = genericSuperclass.getTypeName();
////        Class clazz = genericSuperclass.getClass();
//////        ParameterizedType  p = ((ParameterizedType));
////        return (Class<M>) ((ParameterizedType) genericSuperclass)
////                .getActualTypeArguments()[0];
//    }
}
