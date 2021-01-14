package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class IdentifiableMessageMap<KEY, M extends AbstractMessage, MB extends M.Builder<MB>> extends IdentifiableValueMap<KEY, IdentifiableMessage<KEY, M, MB>> {

    public IdentifiableMessageMap(final Collection<M> messageList) {
        if (messageList == null) {
            return;
        }

        for (final M message : messageList) {
            try {
                put(new IdentifiableMessage<>(message));
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not add Message[" + message + "] to message map!", ex), logger);
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

    public M removeMessage(M message) throws CouldNotPerformException {
        return super.removeValue(new IdentifiableMessage<>(message)).getMessage();
    }
}
