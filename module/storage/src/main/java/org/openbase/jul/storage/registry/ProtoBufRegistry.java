package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
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

import java.util.ArrayList;
import java.util.List;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public interface ProtoBufRegistry<KEY, M extends AbstractMessage, MB extends M.Builder<MB>> extends FileSynchronizedRegistry<KEY, IdentifiableMessage<KEY, M, MB>> {

    M register(final M entry) throws CouldNotPerformException;

    boolean contains(final M key) throws CouldNotPerformException;

    M update(final M entry) throws CouldNotPerformException;

    M remove(final M entry) throws CouldNotPerformException;

    M getMessage(final KEY key) throws CouldNotPerformException;

    default List<M> getMessages() throws CouldNotPerformException {
        final List<M> messageList = new ArrayList<>();
        for (final IdentifiableMessage<KEY, M, MB> identifiableMessage : getEntries()) {
            messageList.add(identifiableMessage.getMessage());
        }
        return messageList;
    }

    MB getBuilder(final KEY key) throws CouldNotPerformException;
}
