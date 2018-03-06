package org.openbase.jul.extension.protobuf.processing;

/*-
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

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class SimpleMessageProcessor<M extends GeneratedMessage> implements MessageProcessor<GeneratedMessage, M> {

    private final Class<M> dataClass;

    public SimpleMessageProcessor(Class<M> dataClass) {
        this.dataClass = dataClass;
    }

    @Override
    public M process(GeneratedMessage input) throws CouldNotPerformException, InterruptedException {
        if (dataClass.isInstance(input)) {
            return (M) input;
        } else {
            throw new CouldNotPerformException("Input does not match output in SimpleMessageProcessor! Expected[" + dataClass.getSimpleName() + "] but was [" + input.getClass().getSimpleName() + "]");
        }
    }

    public Class<M> getDataClass() {
        return dataClass;
    }
}
