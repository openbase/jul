package org.openbase.jul.extension.protobuf.container.transformer;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import org.openbase.jul.extension.protobuf.container.MessageContainer;
import org.openbase.jul.extension.protobuf.processing.ProtoBufFileProcessor;
import java.lang.reflect.InvocationTargetException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M>
 * @param <MB>
 */
public abstract class MessageTransformer<T extends MessageContainer<M>, M extends AbstractMessage, MB extends M.Builder<MB>> implements ProtoBufFileProcessor.TypeToMessageTransformer<T, M, MB> {

    private final Class<M> messageClass;
    
    public MessageTransformer(final Class<M> messageClass) {
        this.messageClass = messageClass;
    }
    
    @Override
    public M transform(final T type) {
        return type.getMessage();
    }

    @Override
    public MB newBuilderForType() throws CouldNotPerformException {
        try {
            Object invoke = messageClass.getMethod("newBuilder").invoke(null);
            return (MB) invoke;
        } catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | NullPointerException ex) {
            throw new CouldNotPerformException("Coult not generate builder out of message class!", ex);
        }
    }

    public Class<M> getMessageClass() {
        return messageClass;
    }
}
