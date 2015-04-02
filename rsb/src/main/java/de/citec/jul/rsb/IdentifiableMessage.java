/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import java.util.logging.Level;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <MOB>
 */
public class IdentifiableMessage<MOB extends GeneratedMessage> extends Observable<MOB> implements Identifiable<String> {

    private static final Logger logger = LoggerFactory.getLogger(IdentifiableMessage.class);
    
    private MOB messageOrBuilder;

    public IdentifiableMessage(MOB messageOrBuilder) {

        this.messageOrBuilder = messageOrBuilder;
    }

    @Override
    public String getId() throws CouldNotPerformException {
        try {
            if (messageOrBuilder == null) {
                throw new NotAvailableException("messageOrBuilder");
            }
            return (String) messageOrBuilder.getField(messageOrBuilder.getDescriptorForType().findFieldByName(FIELD_ID));
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not detect id.");
        }
    }

    public void setMessage(final MOB messageOrBuilder) {
        this.messageOrBuilder = messageOrBuilder;
        try {
            notifyObservers(messageOrBuilder);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(logger, ex);
        }
    }

    public MOB getMessage() {
        return messageOrBuilder;
    }

    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + "[" + getId() + "]";
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not return id value!", ex);
            return getClass().getSimpleName() + "[?]";
        }
    }

}
