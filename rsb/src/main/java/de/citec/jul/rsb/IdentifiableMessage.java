/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import de.citec.jul.rsb.processing.ProtoBufFileProcessor;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.Message;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import java.lang.reflect.ParameterizedType;

/**
 *
 * @author mpohling
 * @param <MOB>
 */
public class IdentifiableMessage<MOB extends GeneratedMessage> implements Identifiable<String> {

    private final MOB messageOrBuilder;

    public IdentifiableMessage(MOB messageOrBuilder) {
        this.messageOrBuilder = messageOrBuilder;
    }

    @Override
    public String getId() {
        return (String) messageOrBuilder.getField(messageOrBuilder.getDescriptorForType().findFieldByName(FIELD_ID));
    }

    public MOB getMessageOrBuilder() {
        return messageOrBuilder;
    }

    @Override
    public String toString() {
        return "Message[" + getId() + "]";
    }
    
    
}
