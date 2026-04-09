package org.openbase.jul.extension.type.processing

import com.google.protobuf.MessageOrBuilder
import org.openbase.jul.exception.*
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.extension.protobuf.processing.ProtoBufFieldProcessor
import org.openbase.type.timing.TimestampType
import org.openbase.type.timing.TimestampType.TimestampOrBuilder
import org.slf4j.Logger
import java.lang.reflect.InvocationTargetException
import java.time.Instant
import java.util.*
import java.util.concurrent.TimeUnit

/**
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
object TimestampProcessor {
    const val SET: String = "set"
    const val TIMESTAMP_NAME: String = "Timestamp"

    @JvmField
    val TIMESTAMP_FIELD_NAME: String = TIMESTAMP_NAME.lowercase()

    @JvmStatic
    val currentTimestamp: TimestampType.Timestamp
        /**
         * Method returns a new Timestamp object with the current system time.
         *
         *
         * Note: The Timestamp type uses microseconds since January 1st, 1970 in UTC time as physical unit.
         *
         * @return the generated timestamp object representing the current time.
         */
        get() = TimestampJavaTimeTransform.transform(System.currentTimeMillis())

    /**
     * Method updates the timestamp field of the given message with the current time if the timestamp is not yet set.
     *
     * @param <M>              the message type of the message to update.
     * @param messageOrBuilder the message
     *
     * @return the updated message
     *
     * @throws CouldNotPerformException is thrown in case the copy could not be performed e.g. because of a missing timestamp field.
    </M> */
    @JvmStatic
    @Throws(CouldNotPerformException::class)
    fun <M : MessageOrBuilder> updateTimestampWithCurrentTimeIfMissing(messageOrBuilder: M): M {
        // skip update if timestamp was already set
        if (hasTimestamp(messageOrBuilder)) {
            return messageOrBuilder
        }

        // update timestamp
        return updateTimestamp(System.currentTimeMillis(), messageOrBuilder)
    }

    /**
     * Method updates the timestamp field of the given message with the current time.
     *
     * @param <M>              the message type of the message to update.
     * @param messageOrBuilder the message
     *
     * @return the updated message
     *
     * @throws CouldNotPerformException is thrown in case the copy could not be performed e.g. because of a missing timestamp field.
    </M> */
    @JvmStatic
    @Throws(CouldNotPerformException::class)
    fun <M : MessageOrBuilder> updateTimestampWithCurrentTime(messageOrBuilder: M): M {
        return updateTimestamp(System.currentTimeMillis(), messageOrBuilder)
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
     * @throws CouldNotPerformException is thrown in case the copy could not be performed e.g. because of a missing timestamp field.
    </M> */
    @JvmStatic
    @Throws(CouldNotPerformException::class)
    fun <M : MessageOrBuilder> updateTimestamp(milliseconds: Long, messageOrBuilder: M): M {
        return updateTimestamp(milliseconds, messageOrBuilder, TimeUnit.MILLISECONDS)
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
     * @throws CouldNotPerformException is thrown in case the copy could not be performed e.g. because of a missing timestamp field.
    </M> */
    @JvmStatic
    @Throws(CouldNotPerformException::class)
    fun <M : MessageOrBuilder> updateTimestamp(time: Long, messageOrBuilder: M?, timeUnit: TimeUnit?): M {
        val milliseconds = TimeUnit.MILLISECONDS.convert(time, timeUnit)

        try {
            if (messageOrBuilder == null) {
                throw NotAvailableException("messageOrBuilder")
            }

            try {
                // handle builder
                if (messageOrBuilder.javaClass.simpleName == "Builder") {
                    messageOrBuilder.javaClass.getMethod(SET + TIMESTAMP_NAME, TimestampType.Timestamp::class.java)
                        .invoke(messageOrBuilder, TimestampJavaTimeTransform.transform(milliseconds))
                    return messageOrBuilder
                }

                //handle message
                val builder: Any = messageOrBuilder.javaClass.getMethod("toBuilder").invoke(messageOrBuilder)
                builder.javaClass.getMethod(SET + TIMESTAMP_NAME, TimestampType.Timestamp::class.java)
                    .invoke(builder, TimestampJavaTimeTransform.transform(milliseconds))
                return builder.javaClass.getMethod("build").invoke(builder) as M
            } catch (ex: IllegalAccessException) {
                throw NotSupportedException("Field[Timestamp]", messageOrBuilder.javaClass.name, ex)
            } catch (ex: IllegalArgumentException) {
                throw NotSupportedException("Field[Timestamp]", messageOrBuilder.javaClass.name, ex)
            } catch (ex: InvocationTargetException) {
                throw NotSupportedException("Field[Timestamp]", messageOrBuilder.javaClass.name, ex)
            } catch (ex: NoSuchMethodException) {
                throw NotSupportedException("Field[Timestamp]", messageOrBuilder.javaClass.name, ex)
            }
        } catch (ex: CouldNotPerformException) {
            throw CouldNotPerformException("Could not update timestemp! ", ex)
        }
    }

    /**
     * Method resolves the timestamp of the given `messageOrBuilder` and returns the time it the given `timeUnit`.
     *
     * @param messageOrBuilder a message containing the timestamp.
     * @param timeUnit         the time unit of the return value.
     *
     * @return the time of the timestamp.
     *
     * @throws NotAvailableException in thrown if the `messageOrBuilder` does not support or contain a timestamp.
     */
    @JvmStatic
    @Throws(NotAvailableException::class)
    fun getTimestamp(messageOrBuilder: MessageOrBuilder, timeUnit: TimeUnit): Long {
        val timeStampFieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(
            messageOrBuilder, TIMESTAMP_NAME.lowercase(
                Locale.getDefault()
            )
        )
        if (timeStampFieldDescriptor == null) {
            throw NotAvailableException(
                TIMESTAMP_NAME,
                VerificationFailedException(messageOrBuilder.descriptorForType.name + " does not provide Field[" + TIMESTAMP_NAME + "]")
            )
        } else if (!messageOrBuilder.hasField(timeStampFieldDescriptor)) {
            throw NotAvailableException(messageOrBuilder.descriptorForType.name, TIMESTAMP_NAME)
        }
        return timeUnit.convert(
            (messageOrBuilder.getField(timeStampFieldDescriptor) as TimestampType.Timestamp).time,
            TimeUnit.MICROSECONDS
        )
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
    </M> */
    @JvmStatic
    fun <M : MessageOrBuilder> updateTimestamp(
        timestamp: Long,
        messageOrBuilder: M,
        timeUnit: TimeUnit?,
        logger: Logger?,
    ): M {
        try {
            return updateTimestamp<M>(timestamp, messageOrBuilder, timeUnit)
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory(ex, logger)
            return messageOrBuilder
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
    </M> */
    fun <M : MessageOrBuilder> updateTimestamp(timestamp: Long, messageOrBuilder: M, logger: Logger?): M {
        try {
            return updateTimestamp(timestamp, messageOrBuilder)
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory(ex, logger)
            return messageOrBuilder
        }
    }

    /**
     * Method copies the timestamp field of the source message into the target message.
     * In case of an error the original message is returned.
     *
     * @param <M>                    the message type of the message to update.
     * @param sourceMessageOrBuilder the message providing a timestamp.
     * @param targetMessageOrBuilder the message offering a timestamp field to update.
     * @param logger                 the logger which is used for printing the exception stack in case something went wrong.
     *
     * @return the updated message or the original one in case of errors.
    </M> */
    fun <M : MessageOrBuilder> copyTimestamp(
        sourceMessageOrBuilder: M,
        targetMessageOrBuilder: M,
        logger: Logger?,
    ): M {
        try {
            return copyTimestamp(sourceMessageOrBuilder, targetMessageOrBuilder)
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory(ex, logger)
            return targetMessageOrBuilder
        }
    }

    /**
     * Method copies the timestamp field of the source message into the target message.
     * In case of an error the original message is returned.
     *
     * @param <M>                    the message type of the message to update.
     * @param sourceMessageOrBuilder the message providing a timestamp.
     * @param targetMessageOrBuilder the message offering a timestamp field to update.
     *
     * @return the updated message or the original one in case of errors.
     *
     * @throws CouldNotPerformException is thrown in case the copy could not be performed e.g. because of a missing timestamp field.
    </M> */
    @JvmStatic
    @Throws(CouldNotPerformException::class)
    fun <M : MessageOrBuilder> copyTimestamp(sourceMessageOrBuilder: M, targetMessageOrBuilder: M): M = updateTimestamp(
        getTimestamp(sourceMessageOrBuilder, TimeUnit.MICROSECONDS),
        targetMessageOrBuilder,
        TimeUnit.MICROSECONDS
    )

    /**
     * Method updates the timestamp field of the given message with the current time.
     * In case of an error the original message is returned.
     *
     * @param <M>              the message type of the message to update.
     * @param messageOrBuilder the message
     * @param logger           the logger which is used for printing the exception stack in case something went wrong.
     *
     * @return the updated message or the original one in case of errors.
    </M> */
    @JvmStatic
    fun <M : MessageOrBuilder> updateTimestampWithCurrentTime(messageOrBuilder: M, logger: Logger?): M {
        try {
            return updateTimestampWithCurrentTime(messageOrBuilder)
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory(ex, logger)
            return messageOrBuilder
        }
    }

    /**
     * Method return true if the given message contains a timestamp field.
     *
     * @param messageOrBuilder the message to analyze
     *
     * @return true if the timestamp field is provided, otherwise false.
     */
    @JvmStatic
    fun hasTimestamp(messageOrBuilder: MessageOrBuilder): Boolean {
        try {
            val fieldDescriptor = ProtoBufFieldProcessor.getFieldDescriptor(messageOrBuilder, TIMESTAMP_FIELD_NAME)
            return messageOrBuilder.hasField(fieldDescriptor)
        } catch (ex: NotAvailableException) {
            return false
        }
    }
}

val <M : TimestampOrBuilder> M.instant: Instant?
    get() = tryOrNull {
        Instant.ofEpochMilli(time.div(1000))
    }
