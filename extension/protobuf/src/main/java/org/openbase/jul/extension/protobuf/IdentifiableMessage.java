package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */
import com.google.protobuf.GeneratedMessage;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.container.MessageContainer;
import org.openbase.jul.iface.Identifiable;
import static org.openbase.jul.iface.provider.LabelProvider.TYPE_FIELD_LABEL;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY> ID Type
 * @param <M> Internal Message
 * @param <MB>
 */
public class IdentifiableMessage<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> implements Identifiable<KEY>, MessageContainer<M> {

    protected final static Logger logger = LoggerFactory.getLogger(IdentifiableMessage.class);
    private static boolean debugMode;

    static {
        try {
            debugMode = JPService.getProperty(JPDebugMode.class).getValue();
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not check debug mode state!", ex), logger, LogLevel.WARN);
            debugMode = false;
        }
    }

    private M internalMessage;
    private ObservableImpl<IdentifiableMessage<KEY, M, MB>> observable;

    /**
     * Copy Constructor
     *
     * @param identifiableMessage
     * @throws InstantiationException
     */
    public IdentifiableMessage(IdentifiableMessage<KEY, M, MB> identifiableMessage) throws InstantiationException {
        this(identifiableMessage.getMessage());
    }

    public IdentifiableMessage(final M message, final IdGenerator<KEY, M> idGenerator) throws InstantiationException {
        try {
            if (idGenerator == null) {
                throw new NotAvailableException("idGenerator");
            }

            if (message == null) {
                throw new NotAvailableException("message");
            }

            this.internalMessage = message;
            this.observable = new ObservableImpl<>();
            this.setupId(idGenerator);
        } catch (CouldNotPerformException ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    public IdentifiableMessage(final M message) throws InstantiationException {
        try {
            if (message == null) {
                throw new NotAvailableException("message");
            }

            this.internalMessage = message;

            if (!verifyId()) {
                throw new InvalidStateException("message does not contain Field[" + TYPE_FIELD_ID + "]");
            }

            this.observable = new ObservableImpl<>();
        } catch (CouldNotPerformException ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }


    @Override
    public KEY getId() throws NotAvailableException {
        return getId(internalMessage);
    }


    public static <KEY> KEY getId(GeneratedMessage message) throws NotAvailableException {
        try {
            if (message == null) {
                throw new NotAvailableException("messageOrBuilder");
            }

            if (message.getDescriptorForType().findFieldByName(TYPE_FIELD_ID) == null) {
                throw new VerificationFailedException("Given message has no id field!");
            }

            if (!message.hasField(message.getDescriptorForType().findFieldByName(TYPE_FIELD_ID))) {
                throw new VerificationFailedException("Given message has no id field!");
            }
            KEY id = (KEY) message.getField(message.getDescriptorForType().findFieldByName(TYPE_FIELD_ID));

            if (id.toString().isEmpty()) {
                throw new VerificationFailedException("Detected id is empty!");
            }

            return id;

        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("id", ex);
        }
    }

    private void setupId(final IdGenerator<KEY, M> generator) throws CouldNotPerformException {
        try {
            if (verifyId()) {
                return;
            }
            if (generator == null) {
                throw new NotAvailableException("idGenerator");
            }
            setId(generator.generateId(internalMessage));
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not setup id for message: " + internalMessage, ex);
        }
    }

    private boolean verifyId() {
        return internalMessage.hasField(internalMessage.getDescriptorForType().findFieldByName(TYPE_FIELD_ID));
    }

    private void setId(final KEY id) throws InvalidStateException, CouldNotPerformException {
        try {
            if (verifyId()) {
                throw new InvalidStateException("ID already specified!");
            }
            setMessage((M) internalMessage.toBuilder().setField(internalMessage.getDescriptorForType().findFieldByName(TYPE_FIELD_ID), id).build());
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
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger);
        }
    }

    @Override
    public M getMessage() {
        return internalMessage;
    }

    public String getMessageTypeName() {
        return internalMessage.getClass().getSimpleName();
    }

    public void addObserver(Observer<IdentifiableMessage<KEY, M, MB>> observer) {
        observable.addObserver(observer);
    }

    public void removeObserver(Observer<IdentifiableMessage<KEY, M, MB>> observer) {
        observable.removeObserver(observer);
    }

    @Override
    public String toString() {
//            try {
//                if (JPService.getProperty(JPVerbose.class).getValue()) {
//                    return getClass().getSimpleName() + "[" + internalMessage + "]";
//                }
//            } catch (JPNotAvailableException ex) {
//                logger.warn("JPVerbose property could not be loaded!");
//            }
        return getMessageTypeName() + "[" + generateMessageDescription() + "]";
    }

    /**
     * Method generates a short string description of the internal message. This description is bases on internal message fields like label or id provided by the message itself.
     * If non of this fields could be detected a ? char is returned.
     *
     * @return a short description of the message as string.
     */
    public String generateMessageDescription() {
        return generateMessageDescription(internalMessage);
    }

    /**
     * Method generates a short string description of the given message. This description is bases on internal message fields like label or id provided by the message itself.
     * If non of this fields could be detected a ? char is returned.
     *
     * @return a short description of the message as string.
     */
    public static String generateMessageDescription(final GeneratedMessage message) {
        if (message.getDescriptorForType().findFieldByName(TYPE_FIELD_LABEL) != null) {
            if (message.hasField(message.getDescriptorForType().findFieldByName(TYPE_FIELD_LABEL))) {
                return (String) message.getField(message.getDescriptorForType().findFieldByName(TYPE_FIELD_LABEL));
            }
        }

        try {
            return getId(message).toString();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not detect id value of internal message!", ex), logger, LogLevel.WARN);
            return "?";
        }
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder()
                .append(internalMessage)
                .toHashCode();
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj instanceof IdentifiableMessage) {
            final IdentifiableMessage other = (IdentifiableMessage) obj;
            return new EqualsBuilder()
                    .append(internalMessage, other.internalMessage)
                    .isEquals();
        } else {
            return false;
        }
    }
}
