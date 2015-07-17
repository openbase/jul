/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.iface.Identifiable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class ProtobufListDiff<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> {

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(ProtobufListDiff.class);

    private IdentifiableMessageMap<KEY, M, MB> newMessages, updatedMessages, removedMessages, originalMessages;

    public ProtobufListDiff(final List<M> originalList) {
        this();
        this.originalMessages.putAll(new IdentifiableMessageMap(originalList));
    }

    public ProtobufListDiff(IdentifiableMessageMap<KEY, M, MB> originalMap) {
        this();
        this.originalMessages.putAll(originalMap);

    }

    public ProtobufListDiff() {
        this.newMessages = new IdentifiableMessageMap();
        this.updatedMessages = new IdentifiableMessageMap();
        this.removedMessages = new IdentifiableMessageMap();
        this.originalMessages = new IdentifiableMessageMap();
    }

    public void diff(final List<M> modifieredList) {
        diff(new IdentifiableMessageMap(modifieredList));
    }

    public void diff(final List<M> originalList, final List<M> modifieredList) {
        diff(new IdentifiableMessageMap(originalList), new IdentifiableMessageMap(modifieredList));
    }

    public void diff(final IdentifiableMessageMap<KEY, M, MB> modifieredMap) {
        diff(originalMessages, modifieredMap);
    }

    public void diff(final IdentifiableMessageMap<KEY, M, MB> originalMap, final IdentifiableMessageMap<KEY, M, MB> modifieredMap) {
        newMessages.clear();
        updatedMessages.clear();
        removedMessages.clear();

        final IdentifiableMessageMap<KEY, M, MB> modifieredListCopy = new IdentifiableMessageMap<>(modifieredMap);

        for (KEY id : originalMap.keySet()) {
            try {
                if (modifieredMap.containsKey(id)) {
                    if (!originalMap.get(id).getMessage().equals(modifieredMap.get(id).getMessage())) {
                        updatedMessages.put(modifieredMap.get(id));
                    }
                    modifieredListCopy.remove(id);
                } else {
                    removedMessages.put(originalMap.get(id));
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Ignoring invalid Message[" + id + "]", ex));
            }
        }
        // add all messages which are not included in original list.
        newMessages.putAll(modifieredListCopy);
        
        // update original messages.
        originalMessages = modifieredMap;
    }

    public IdentifiableMessageMap<KEY, M, MB> getNewMessageMap() {
        return newMessages;
    }

    public IdentifiableMessageMap<KEY, M, MB> getUpdatedMessageMap() {
        return updatedMessages;
    }

    public IdentifiableMessageMap<KEY, M, MB> getRemovedMessageMap() {
        return removedMessages;
    }

    public class IdentifiableMessageMap<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends IdentifiableValueMap<KEY, IdentifiableMessage<KEY, M, MB>> {

        protected final Logger logger = LoggerFactory.getLogger(IdentifiableMessageMap.class);

        public IdentifiableMessageMap(List<M> messageList) {

            if (messageList == null) {
                return;
            }

            for (M message : messageList) {
                try {
                    put(new IdentifiableMessage<KEY, M, MB>(message));
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not add Message[" + message + "] to message map!", ex));
                }
            }
        }

        public IdentifiableMessageMap(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        public IdentifiableMessageMap(int initialCapacity) {
            super(initialCapacity);
        }

        public IdentifiableMessageMap() {
        }

        public IdentifiableMessageMap(Map<? extends KEY, ? extends IdentifiableMessage<KEY, M, MB>> m) {
            super(m);
        }

        public Set<M> getMessages() {
            Set<M> messages = new HashSet<>();
            for (IdentifiableMessage<KEY, M, MB> identifiableMessage : values()) {
                messages.add(identifiableMessage.getMessage());
            }
            return messages;
        }
    }

    public class IdentifiableValueMap<KEY, VALUE extends Identifiable<KEY>> extends HashMap<KEY, VALUE> {

        protected final Logger logger = LoggerFactory.getLogger(IdentifiableValueMap.class);

        public IdentifiableValueMap(int initialCapacity, float loadFactor) {
            super(initialCapacity, loadFactor);
        }

        public IdentifiableValueMap(int initialCapacity) {
            super(initialCapacity);
        }

        public IdentifiableValueMap() {
        }

        public IdentifiableValueMap(Map<? extends KEY, ? extends VALUE> m) {
            super(m);
        }

        public void put(final VALUE value) throws CouldNotPerformException {
            try {
                put(value.getId(), value);
            } catch (CouldNotPerformException ex) {
                throw new CouldNotPerformException("Could not put value to list!", ex);
            }
        }
    }
}
