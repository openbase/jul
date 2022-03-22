package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Message;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException.ContextType;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.container.MessageContainer;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.provider.LabelProvider;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.type.language.LabelType.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <KEY> ID Type
 * @param <M>   Internal Message
 * @param <MB>
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class IdentifiableMessage<KEY, M extends AbstractMessage, MB extends M.Builder> implements Identifiable<KEY>, MessageContainer<M> {

    protected final static Logger logger = LoggerFactory.getLogger(IdentifiableMessage.class);

    private M internalMessage;
    private ObservableImpl<Object, IdentifiableMessage<KEY, M, MB>> observable;

    /**
     * Copy Constructor
     *
     * @param identifiableMessage
     *
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
            this.observable = new ObservableImpl<>(this);
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

    public static <KEY> KEY getId(Message message) throws NotAvailableException {
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
            throw new NotAvailableException(ContextType.USE_ID_AS_CONTEXT, "id", ex);
        }
    }

    /**
     * Method generates a short string description of the given message. This description is bases on internal message fields like label or id provided by the message itself.
     * If non of this fields could be detected a ? char is returned.
     *
     * @return a short description of the message as string.
     */
    public static String generateMessageDescription(final Message message) {

        String label = null;
        String alias = null;

        // resolve label and return it as description of this message.
        final FieldDescriptor labelFieldDescriptor = message.getDescriptorForType().findFieldByName(LabelProvider.TYPE_FIELD_LABEL);
        if (labelFieldDescriptor != null) {
            if (message.hasField(labelFieldDescriptor)) {
                Label labelType = (Label) message.getField(labelFieldDescriptor);
                if (labelType != null && labelType.getEntryCount() > 0 && labelType.getEntry(0).getValueCount() > 0) {
                    label = labelType.getEntry(0).getValue(0);
                }
            }
        }

        // resolve alias and return it as description of this message.
        final FieldDescriptor aliasFieldDescriptor = message.getDescriptorForType().findFieldByName("alias");
        if (aliasFieldDescriptor != null) {
            if (message.getRepeatedFieldCount(aliasFieldDescriptor) > 0) {
                alias = (String) message.getRepeatedField(aliasFieldDescriptor, 0);
            }
        }

        // generate description
        if (label != null || alias != null) {
            if (label == null) {
                return alias;
            } else if (alias == null) {
                return label;
            } else {
                return alias + ":" + label;
            }
        }

        // use id as fallback if available.
        try {
            return getId(message).toString();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not detect id value of internal message!", ex), logger, LogLevel.WARN);
            return "?";
        }
    }

    @Override
    public KEY getId() throws NotAvailableException {
        return getId(internalMessage);
    }

    private void setId(final KEY id) throws CouldNotPerformException {
        try {
            if (verifyId()) {
                throw new InvalidStateException("ID already specified!");
            }
            setMessage((M) internalMessage.toBuilder().setField(internalMessage.getDescriptorForType().findFieldByName(TYPE_FIELD_ID), id).build());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not setup id!", ex);
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

    /**
     * Updates the message of this instance.
     *
     * @param builder the message builder delivering the new message.
     *
     * @return the updated message is returned.
     *
     * @throws CouldNotPerformException in thrown in case something went wrong during message processing.
     * @deprecated since v2.0 and will be removed in v3.0. Please use setMessage(final MB builder, final Object source) instead.
     */
    @Deprecated
    public IdentifiableMessage<KEY, M, MB> setMessage(final MB builder) throws CouldNotPerformException {
        return setMessage((M) builder.build(), this);
    }

    /**
     * Updates the message of this instance.
     *
     * @param builder the message builder delivering the new message.
     * @param source  the responsible source of the new message.
     *
     * @return the updated message is returned.
     *
     * @throws CouldNotPerformException in thrown in case something went wrong during message processing.
     */
    public IdentifiableMessage<KEY, M, MB> setMessage(final MB builder, final Object source) throws CouldNotPerformException {
        if (builder == null) {
            throw new NotAvailableException("message");
        }
        return setMessage((M) builder.build(), source);
    }

    /**
     * Updates the message of this instance.
     *
     * @param message the new message object used as replacement for the old message.
     * @param source  the responsible source of the new message.
     *
     * @return the updated message is returned.
     *
     * @throws CouldNotPerformException in thrown in case something went wrong during message processing.
     */
    public IdentifiableMessage<KEY, M, MB> setMessage(final M message, final Object source) throws CouldNotPerformException {
        if (message == null) {
            throw new NotAvailableException("message");
        }
        this.internalMessage = message;
        notifyObservers(source);
        return this;
    }

    private void notifyObservers(final Object source) {
        try {
            observable.notifyObservers(source, this);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger);
        }
    }

    @Override
    public M getMessage() {
        return internalMessage;
    }

    /**
     * Updates the message of this instance.
     *
     * @param message the new message.
     *
     * @return the updated message is returned.
     *
     * @throws CouldNotPerformException in thrown in case something went wrong during message processing.
     * @deprecated since v2.0 and will be removed in v3.0. Please use setMessage(final M message, final Object source) instead.
     */
    @Deprecated
    public IdentifiableMessage<KEY, M, MB> setMessage(final M message) throws CouldNotPerformException {
        return setMessage(message, this);
    }

    public String getMessageTypeName() {
        return internalMessage.getClass().getSimpleName();
    }

    public void addObserver(Observer<Object, IdentifiableMessage<KEY, M, MB>> observer) {
        observable.addObserver(observer);
    }

    public void removeObserver(Observer<Object, IdentifiableMessage<KEY, M, MB>> observer) {
        observable.removeObserver(observer);
    }

    @Override
    public String toString() {
        return getMessageTypeName() + "[" + generateMessageDescription() + "]";
    }

    /**
     * Method generates a short string description of the internal message. This description is bases on internal message fields like alias or id provided by the message itself.
     * If non of this fields could be detected a ? char is returned.
     *
     * @return a short description of the message as string.
     */
    public String generateMessageDescription() {
        return generateMessageDescription(internalMessage);
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
