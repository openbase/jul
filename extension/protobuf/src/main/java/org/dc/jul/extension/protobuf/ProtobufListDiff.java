/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import java.util.List;
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

    private IdentifiableMessageMap<KEY, M, MB> newMessages, updatedMessages, removedMessages, originMessages;

    public ProtobufListDiff(final List<M> originMessages) {
        this();
        this.originMessages.putAll(new IdentifiableMessageMap<>(originMessages));
    }

    public ProtobufListDiff(IdentifiableMessageMap<KEY, M, MB> originMessages) {
        this();
        this.originMessages.putAll(originMessages);

    }

    public ProtobufListDiff() {
        this.newMessages = new IdentifiableMessageMap<>();
        this.updatedMessages = new IdentifiableMessageMap<>();
        this.removedMessages = new IdentifiableMessageMap<>();
        this.originMessages = new IdentifiableMessageMap<>();
    }

    public void diff(final List<M> modifieredList) {
        diff(new IdentifiableMessageMap<>(modifieredList));
    }

    public void diff(final List<M> originalList, final List<M> modifieredList) {
        diff(new IdentifiableMessageMap<>(originalList), new IdentifiableMessageMap<>(modifieredList));
    }

    public void diff(final IdentifiableMessageMap<KEY, M, MB> modifieredMap) {
        diff(originMessages, modifieredMap);
    }

    public void diff(final IdentifiableMessageMap<KEY, M, MB> originalMap, final IdentifiableMessageMap<KEY, M, MB> modifieredMap) {
        newMessages.clear();
        updatedMessages.clear();
        removedMessages.clear();

        final IdentifiableMessageMap<KEY, M, MB> modifieredListCopy = new IdentifiableMessageMap<>(modifieredMap);

        originalMap.keySet().stream().forEach((id) -> {
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
                ExceptionPrinter.printHistory(new CouldNotPerformException("Ignoring invalid Message[" + id + "]", ex), logger, LogLevel.ERROR);
            }
        });
        // add all messages which are not included in original list.
        newMessages.putAll(modifieredListCopy);

        // update original messages.
        originMessages = modifieredMap;
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

    public int getChangeCounter() {
        return newMessages.size() + updatedMessages.size() + removedMessages.size();
    }

    public void replaceOriginMap(IdentifiableMessageMap<KEY, M, MB> originMap) {
        originMessages.clear();
        originMessages.putAll(originMap);
    }

    public IdentifiableMessageMap<KEY, M, MB> getOriginMessages() {
        return originMessages;
    }
}
