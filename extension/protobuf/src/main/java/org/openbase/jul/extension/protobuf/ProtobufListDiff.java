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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtobufListDiff<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> {

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(ProtobufListDiff.class);

    private IdentifiableMessageMap<KEY, M, MB> newMessages, updatedMessages, removedMessages, originalMessages;

    public ProtobufListDiff(final List<M> originalMessages) {
        this();
        this.originalMessages.putAll(new IdentifiableMessageMap<>(originalMessages));
    }

    public ProtobufListDiff(IdentifiableMessageMap<KEY, M, MB> originalMessages) {
        this();
        this.originalMessages.putAll(originalMessages);

    }

    public ProtobufListDiff() {
        this.newMessages = new IdentifiableMessageMap<>();
        this.updatedMessages = new IdentifiableMessageMap<>();
        this.removedMessages = new IdentifiableMessageMap<>();
        this.originalMessages = new IdentifiableMessageMap<>();
    }

    public void diff(final List<M> modifiedList) {
        diff(new IdentifiableMessageMap<>(modifiedList));
    }

    public void diff(final List<M> originalList, final List<M> modifiedList) {
        diff(new IdentifiableMessageMap<>(originalList), new IdentifiableMessageMap<>(modifiedList));
    }

    public void diff(final IdentifiableMessageMap<KEY, M, MB> modifiedMap) {
        diff(originalMessages, modifiedMap);
    }

    public void diff(final IdentifiableMessageMap<KEY, M, MB> originalMap, final IdentifiableMessageMap<KEY, M, MB> modifiedMap) {
        newMessages.clear();
        updatedMessages.clear();
        removedMessages.clear();

        final IdentifiableMessageMap<KEY, M, MB> modifiedListCopy = new IdentifiableMessageMap<>(modifiedMap);

        originalMap.keySet().stream().forEach((id) -> {
            try {
                if (modifiedMap.containsKey(id)) {
                    if (!originalMap.get(id).getMessage().equals(modifiedMap.get(id).getMessage())) {
                        updatedMessages.put(modifiedMap.get(id));
                    }
                    modifiedListCopy.remove(id);
                } else {
                    removedMessages.put(originalMap.get(id));
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Ignoring invalid Message[" + id + "]", ex), logger, LogLevel.ERROR);
            }
        });
        // add all messages which are not included in original list.
        newMessages.putAll(modifiedListCopy);

        // update original messages.
        originalMessages = modifiedMap;
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

    /**
     *
     * @param originMap
     * @deprecated  use replaceOriginalMap instead
     */
    @Deprecated
    public void replaceOriginMap(IdentifiableMessageMap<KEY, M, MB> originMap) {
        replaceOriginalMap(originMap);
    }

    public void replaceOriginalMap(IdentifiableMessageMap<KEY, M, MB> originalMap) {
        originalMessages.clear();;
        originalMessages.putAll(originalMap);
    }

    /**
     *
     * @return
     * @deprecated use getOriginalMessages instead
     */
    public IdentifiableMessageMap<KEY, M, MB> getOriginMessages() {
        return getOriginalMessages();
    }

    public IdentifiableMessageMap<KEY, M, MB> getOriginalMessages() {
        return originalMessages;
    }
}
