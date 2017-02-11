package org.openbase.jul.extension.rst.processing;

import com.google.protobuf.GeneratedMessage;
import java.lang.reflect.InvocationTargetException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotSupportedException;
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
     * @param message the message
     * @return
     * @throws CouldNotPerformException
     */
    public static <M extends GeneratedMessage> M updateTimeStampWithCurrentTime(final M message) throws CouldNotPerformException {
        try {
            message.getClass().getMethod(SET + TIMESTEMP_FIELD, Timestamp.class).invoke(message, getCurrentTimestamp());
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            throw new CouldNotPerformException("Could not update timestemp! ", new NotSupportedException("Field[Timestamp]", message.getClass().getName(), ex));
        }
        return message;
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
    public static <M extends GeneratedMessage> M updateTimeStamp(final long millisecunds, final M message) throws CouldNotPerformException {
        try {
            message.getClass().getMethod(SET + TIMESTEMP_FIELD, Timestamp.class).invoke(message, TimestampJavaTimeTransform.transform(millisecunds));
        } catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException | NoSuchMethodException ex) {
            throw new CouldNotPerformException("Could not update timestemp! ", new NotSupportedException("Field[Timestamp]", message.getClass().getName(), ex));
        }
        return message;
    }
}
