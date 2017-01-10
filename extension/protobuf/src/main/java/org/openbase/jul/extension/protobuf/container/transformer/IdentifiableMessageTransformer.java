package org.openbase.jul.extension.protobuf.container.transformer;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.extension.protobuf.IdGenerator;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class IdentifiableMessageTransformer<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends MessageTransformer<IdentifiableMessage<KEY, M, MB>, M, MB> {

    private final IdGenerator<KEY, M> idGenerator;

    public IdentifiableMessageTransformer(final Class<M> messageClass, final IdGenerator<KEY, M> idGenerator) {
        super(messageClass);
        this.idGenerator = idGenerator;
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> transform(final M message) throws CouldNotTransformException {
        try {
            return new IdentifiableMessage<>(message, idGenerator);
        } catch(org.openbase.jul.exception.InstantiationException ex) {
            throw new CouldNotTransformException("Given message is invalid!" , ex);
        }
    }
}
