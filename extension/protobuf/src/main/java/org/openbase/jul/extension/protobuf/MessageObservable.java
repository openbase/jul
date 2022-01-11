package org.openbase.jul.extension.protobuf;

/*-
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

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;
import com.google.protobuf.Message.Builder;
import org.openbase.jul.pattern.ObservableImpl;

/**
 * This class computes the hashCode to check if the observed value has changed invariant of
 * its timestamp.
 * Currently for efficiency reasons the timestamp of messages in repeated fields is still considered.
 *
 * @param <M> the type which is notified by this observable
 * @param <S> the source type of this observable
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class MessageObservable<S, M extends Message> extends ObservableImpl<S, M> {

    public static final String TIMESTAMP_MESSAGE_NAME = "Timestamp";

    public MessageObservable(final S source) {
        super(source);
        this.setHashGenerator((M value) -> removeTimestamps(value.toBuilder()).build().hashCode());
    }

    /**
     * Recursively clear timestamp messages from a builder. For efficiency repeated fields are ignored.
     *
     * @param builder the builder from which all timestamps are cleared
     * @return the updated builder
     */
    public Builder removeTimestamps(final Builder builder) {
        final Descriptors.Descriptor descriptorForType = builder.getDescriptorForType();
        for (final Descriptors.FieldDescriptor field : descriptorForType.getFields()) {

            // if the field is not repeated, a message and a timestamp it is cleared
            if (!field.isRepeated() && field.getType() == Descriptors.FieldDescriptor.Type.MESSAGE) {
                if (field.getMessageType().getName().equals(TIMESTAMP_MESSAGE_NAME)) {
                    builder.clearField(field);
                } else {

                    // skip checking recursively if the field is not even initialized
                    if (builder.hasField(field)) {
                        removeTimestamps(builder.getFieldBuilder(field));
                    }
                }
            }
        }
        return builder;
    }
}
