/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.VerificationFailedException;
import de.citec.jul.extension.protobuf.container.MessageContainer;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <KEY> ID Type
 * @param <M> Internal Message
 * @param <MB>
 */
public class IdentifiableMessage<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> implements Identifiable<KEY>, MessageContainer<M> {

    protected final static Logger logger = LoggerFactory.getLogger(IdentifiableMessage.class);

    private M internalMessage;

    private Observable<IdentifiableMessage<KEY, M, MB>> observable;

    public IdentifiableMessage(final M message, final IdGenerator<KEY, M> idGenerator) throws InstantiationException {
        try {
            if (idGenerator == null) {
                throw new NotAvailableException("idGenerator");
            }

            if (message == null) {
                throw new NotAvailableException("message");
            }

            this.internalMessage = message;
            this.observable = new Observable<>();
            this.setupId(idGenerator);
        } catch (CouldNotPerformException ex) {
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }
    
    public IdentifiableMessage(final M message) throws InstantiationException {
        try {
            if (message == null) {
                throw new NotAvailableException("message");
            }
            
            this.internalMessage = message;
            
            if(!verifyId()) {
                throw new InvalidStateException("message does not contain Field["+FIELD_ID+"]");
            }

            this.observable = new Observable<>();
        } catch (CouldNotPerformException ex) {
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }

    @Override
    public KEY getId() throws CouldNotPerformException {
        try {
            if (internalMessage == null) {
                throw new NotAvailableException("messageOrBuilder");
            }
            if (!internalMessage.hasField(internalMessage.getDescriptorForType().findFieldByName(FIELD_ID))) {
                throw new VerificationFailedException("Given message has no id field!");
            }
            KEY id = (KEY) internalMessage.getField(internalMessage.getDescriptorForType().findFieldByName(FIELD_ID));

            if (id.toString().isEmpty()) {
                throw new VerificationFailedException("Detected id is empty!");
            }

            return id;

        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not detect id.", ex);
        }
    }

    private void setupId(final IdGenerator<KEY, M> generator) throws CouldNotPerformException {
        try {
            if (verifyId()) {
                return;
            }
            setId(generator.generateId(internalMessage));
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not setup id for message: " + internalMessage, ex);
        }
    }
    
    private boolean verifyId() {
        return internalMessage.hasField(internalMessage.getDescriptorForType().findFieldByName(FIELD_ID));
    }

    private void setId(final KEY id) throws InvalidStateException, CouldNotPerformException {
        try {
            if (verifyId()) {
                throw new InvalidStateException("ID already specified!");
            }
            setMessage((M) internalMessage.toBuilder().setField(internalMessage.getDescriptorForType().findFieldByName(FIELD_ID), id).build());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not setup id!", ex);
        }
    }

    public IdentifiableMessage<KEY, M, MB> setMessage(final MB builder) throws CouldNotPerformException {
        if (builder == null) {
            throw new NotAvailableException("message");
        }
        return setMessage((M) builder.build());
    }

    public IdentifiableMessage<KEY, M, MB> setMessage(final M message) throws CouldNotPerformException {
        if (message == null) {
            throw new NotAvailableException("message");
        }
        this.internalMessage = message;
        notifyObservers();
        return this;
    }

    public void notifyObservers() {
        try {
            observable.notifyObservers(this);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistoryAndReturnThrowable(logger, ex);
        }
    }

    @Override
    public M getMessage() {
        return internalMessage;
    }

    public void addObserver(Observer<IdentifiableMessage<KEY, M, MB>> observer) {
        observable.addObserver(observer);
    }

    public void removeObserver(Observer<IdentifiableMessage<KEY, M, MB>> observer) {
        observable.removeObserver(observer);
    }

    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + "[" + getId().toString() + "]";
        } catch (CouldNotPerformException ex) {
            logger.warn("Could not return id value!", ex);
            return getClass().getSimpleName() + "[?]";
        }
    }

    @Override
    public int hashCode() {
        try {
            return new HashCodeBuilder()
                    .append(getId())
                    .toHashCode();
        } catch (CouldNotPerformException ex) {
            return new HashCodeBuilder()
                    .append(internalMessage)
                    .toHashCode();
        }
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IdentifiableMessage) {
            final IdentifiableMessage other = (IdentifiableMessage) obj;
            try {
                return new EqualsBuilder()
                        .append(getId(), other.getId())
                        .isEquals();
            } catch (CouldNotPerformException ex) {
                return new EqualsBuilder()
                        .append(internalMessage, other.internalMessage)
                        .isEquals();
            }
        } else {
            return false;
        }
    }
}
