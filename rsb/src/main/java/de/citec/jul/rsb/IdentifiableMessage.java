/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.MessageOrBuilder;
import de.citec.jul.iface.Identifiable;

/**
 *
 * @author mpohling
 * @param <MOB>
 */
public class IdentifiableMessage<MOB extends MessageOrBuilder> implements Identifiable<String> {
    
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
}
