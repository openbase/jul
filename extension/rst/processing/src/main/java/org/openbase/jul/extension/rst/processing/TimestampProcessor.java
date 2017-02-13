package org.openbase.jul.extension.rst.processing;

/*-
 * #%L
 * JUL Extension RST Processing
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
import com.google.protobuf.MessageOrBuilder;
import java.lang.reflect.InvocationTargetException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotSupportedException;
import rst.configuration.EntryType.Entry.Builder;
import rst.timing.TimestampType.Timestamp;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class TimestampProcessor {

    public static final String SET = "set";
    public static final String TIMESTEMP_FIELD = "Timestamp";

    /**
     * Method returns a new Timestamp object with the current system time.
     *
     * Note: The Timestamp type uses microseconds since January 1st, 1970 in UTC time as physical unit.
     *
     * @return the generated timestamp object representing the current time.
     */
    public static Timestamp getCurrentTimestamp() {
        return TimestampJavaTimeTransform.transform(System.currentTimeMillis());
    }

    /**
     * Method updates the timestamp field of the given message with the current time.
     *
     * @param <M> the message type of the message to update.
     * @param messageOrBuilder the message
     * @return
     * @throws CouldNotPerformException
     */
    public static <M extends MessageOrBuilder> M updateTimeStampWithCurrentTime(final M messageOrBuilder) throws CouldNotPerformException {
        try {

            // handle builder
            if (messageOrBuilder.getClass().getSimpleName().equals("Builder")) {
                messageOrBuilder.getClass().getMethod(SET + TIMESTEMP_FIELD, Timestamp.class).invoke(messageOrBuilder, getCurrentTimestamp());
                return messageOrBuilder;
            }

            //handle message
            final Object builder = messageOrBuilder.getClass().getMethod("toBuilder").invoke(messageOrBuilder);
            builder.getClass().getMethod(SET + TIMESTEMP_FIELD, Timestamp.class).invoke(builder, getCurrentTimestamp());
            return (M) builder.getClass().getMethod("build").invoke(builder);

        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            throw new CouldNotPerformException("Could not update timestemp! ", new NotSupportedException("Field[Timestamp]", messageOrBuilder.getClass().getName(), ex));
        }
    }

    /**
     * Method updates the timestamp field of the given message with the given timestamp.
     *
     * @param <M> the message type of the message to update.
     * @param millisecunds the time to update
     * @param message the message
     * @return
     * @throws CouldNotPerformException
     */
    public static <M extends MessageOrBuilder> M updateTimeStamp(final long millisecunds, final M message) throws CouldNotPerformException {
        try {
            message.getClass().getMethod(SET + TIMESTEMP_FIELD, Timestamp.class).invoke(message, TimestampJavaTimeTransform.transform(millisecunds));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            throw new CouldNotPerformException("Could not update timestemp! ", new NotSupportedException("Field[Timestamp]", message.getClass().getName(), ex));
        }
        return message;
    }
}
