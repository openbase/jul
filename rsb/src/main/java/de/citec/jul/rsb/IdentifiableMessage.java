/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.iface.Identifiable;

/**
 *
 * @author mpohling
 * @param <MOB>
 */
public class IdentifiableMessage<MOB extends GeneratedMessage> implements Identifiable<String> {

    private MOB messageOrBuilder;

    public IdentifiableMessage(MOB messageOrBuilder) {
        this.messageOrBuilder = messageOrBuilder;
    }

    @Override
    public String getId() {
        return (String) messageOrBuilder.getField(messageOrBuilder.getDescriptorForType().findFieldByName(FIELD_ID));
    }

    public void setMessageOrBuilder(MOB messageOrBuilder) {
        this.messageOrBuilder = messageOrBuilder;
    }

    public MOB getMessageOrBuilder() {
        return messageOrBuilder;
    }

    @Override
    public String toString() {
        return "Message[" + getId() + "]";
    }

}
