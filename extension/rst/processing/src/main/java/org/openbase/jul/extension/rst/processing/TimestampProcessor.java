package org.openbase.jul.extension.rst.processing;

/*-
 * #%L
 * JUL Extension RST Processing
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

import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.MessageOrBuilder;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.NotSupportedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.processing.ProtoBufFieldProcessor;
import org.slf4j.Logger;
import rst.timing.TimestampType.Timestamp;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class TimestampProcessor {

    public static final String SET = "set";
    public static final String TIMESTEMP_NAME = "Timestamp";
    public static final String TIMESTEMP_FIELD_NAME = TIMESTEMP_NAME.toLowerCase();

    /**
     * Method returns a new Timestamp object with the current system time.
     * <p>
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
     * @param <M>              the message type of the message to update.
     * @param messageOrBuilder the message
     *
     * @return the updated message
     *
     * @throws CouldNotPerformException
     */
    public static <M extends MessageOrBuilder> M updateTimestampWithCurrentTime(final M messageOrBuilder) throws CouldNotPerformException {
        return updateTimestamp(System.currentTimeMillis(), messageOrBuilder);
    }

    /**
     * Method updates the timestamp field of the given message with the given timestamp.
     *
     * @param <M>              the message type of the message to milliseconds.
     * @param milliseconds     the time to update
     * @param messageOrBuilder the message
     *
     * @return the updated message
     *
     * @throws CouldNotPerformException
     */
    public static <M extends MessageOrBuilder> M updateTimestamp(final long milliseconds, final M messageOrBuilder) throws CouldNotPerformException {
        return updateTimestamp(milliseconds, messageOrBuilder, TimeUnit.MILLISECONDS);
    }

    /**
     * Method updates the timestamp field of the given message with the given time in the given timeUnit.
     *
     * @param <M>              the message type of the message which is updated
     * @param time             the time which is put in the timestamp field
     * @param messageOrBuilder the message
     * @param timeUnit         the unit of time
     *
     * @return the updated message
     *
     * @throws CouldNotPerformException
     */
    public static <M extends MessageOrBuilder> M updateTimestamp(final long time, final M messageOrBuilder, final TimeUnit timeUnit) throws CouldNotPerformException {
        long milliseconds = TimeUnit.MILLISECONDS.convert(time, timeUnit);

        try {

            if (messageOrBuilder == null) {
                throw new NotAvailableException("messageOrBuilder");
            }

            try {
                // handle builder
                if (messageOrBuilder.getClass().getSimpleName().equals("Builder")) {
                    messageOrBuilder.getClass().getMethod(SET + TIMESTEMP_NAME, Timestamp.class).invoke(messageOrBuilder, TimestampJavaTimeTransform.transform(milliseconds));
                    return messageOrBuilder;
                }

                //handle message
                final Object builder = messageOrBuilder.getClass().getMethod("toBuilder").invoke(messageOrBuilder);
                builder.getClass().getMethod(SET + TIMESTEMP_NAME, Timestamp.class).invoke(builder, TimestampJavaTimeTransform.transform(milliseconds));
                return (M) builder.getClass().getMethod("build").invoke(builder);
            } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
                throw new NotSupportedException("Field[Timestamp]", messageOrBuilder.getClass().getName(), ex);
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not update timestemp! ", ex);
        }
    }

    /**
     * Method resolves the timestamp of the given {@code messageOrBuilder} and returns the time it the given {@code timeUnit}.
     *
     * @param messageOrBuilder a message containing the timestamp.
     * @param timeUnit         the time unit of the return value.
     *
     * @return the time of the timestamp.
     *
     * @throws NotAvailableException in thrown if the {@code messageOrBuilder} does not support or contain a timestamp.
     */
    public static long getTimestamp(final MessageOrBuilder messageOrBuilder, final TimeUnit timeUnit) throws NotAvailableException {
        final FieldDescriptor timeStampFieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(messageOrBuilder, TIMESTEMP_NAME.toLowerCase());
        if (timeStampFieldDescriptor == null || !messageOrBuilder.hasField(timeStampFieldDescriptor)) {
            throw new NotAvailableException(TIMESTEMP_NAME);
        }
        return TimeUnit.MILLISECONDS.convert(((Timestamp) messageOrBuilder.getField(timeStampFieldDescriptor)).getTime(), timeUnit);
    }

    /**
     * Method updates the timestamp field of the given message with the given timestamp.
     * In case of an error the original message is returned.
     *
     * @param <M>              the message type of the message to update.
     * @param timestamp        the timestamp to update
     * @param messageOrBuilder the message
     * @param timeUnit         the timeUnit of the timeStamp
     * @param logger           the logger which is used for printing the exception stack in case something went wrong.
     *
     * @return the updated message or the original one in case of errors.
     */
    public static <M extends MessageOrBuilder> M updateTimestamp(final long timestamp, final M messageOrBuilder, final TimeUnit timeUnit, final Logger logger) {
        try {
            return updateTimestamp(timestamp, messageOrBuilder, timeUnit);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger);
            return messageOrBuilder;
        }
    }

    /**
     * Method updates the timestamp field of the given message with the given timestamp.
     * In case of an error the original message is returned.
     *
     * @param <M>              the message type of the message to update.
     * @param timestamp        the timestamp to update
     * @param messageOrBuilder the message
     * @param logger           the logger which is used for printing the exception stack in case something went wrong.
     *
     * @return the updated message or the original one in case of errors.
     */
    public static <M extends MessageOrBuilder> M updateTimestamp(final long timestamp, final M messageOrBuilder, final Logger logger) {
        try {
            return updateTimestamp(timestamp, messageOrBuilder);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger);
            return messageOrBuilder;
        }
    }

    /**
     * Method updates the timestamp field of the given message with the current time.
     * In case of an error the original message is returned.
     *
     * @param <M>              the message type of the message to update.
     * @param messageOrBuilder the message
     * @param logger           the logger which is used for printing the exception stack in case something went wrong.
     *
     * @return the updated message or the original one in case of errors.
     */
    public static <M extends MessageOrBuilder> M updateTimestampWithCurrentTime(final M messageOrBuilder, final Logger logger) {
        try {
            return updateTimestampWithCurrentTime(messageOrBuilder);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(ex, logger);
            return messageOrBuilder;
        }
    }

    public static boolean hasTimestamp(final MessageOrBuilder messageOrBuilder) {
        try {
            final FieldDescriptor fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(messageOrBuilder, TIMESTEMP_FIELD_NAME);
            return messageOrBuilder.hasField(fieldDescriptor);
        } catch (NotAvailableException ex) {
            return false;
        }
    }
}
