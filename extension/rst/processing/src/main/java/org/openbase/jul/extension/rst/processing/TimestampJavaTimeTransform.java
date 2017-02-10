package org.openbase.jul.extension.rst.processing;

import rst.timing.TimestampType.Timestamp;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class TimestampJavaTimeTransform {

    public static final long FROM_MILLI_TO_MICRO_MULTIPLIER = 1000;

    /**
     * Method transforms the given time in milliseconds into an Timestamp object.
     *
     * @param millisecunds the time in milliseconds.
     * @return a timestamp object representing the given time.
     */
    public static Timestamp transform(final long millisecunds) {
        return Timestamp.newBuilder().setTime(millisecunds * FROM_MILLI_TO_MICRO_MULTIPLIER).build();
    }

    /**
     * Method extracts the milliseconds of the given timestamp.
     *
     * @param timestamp the timestamp to readout the milliseconds.
     * @return the time in milliseconds.
     */
    public static long transform(final Timestamp timestamp) {
        return timestamp.getTime() / FROM_MILLI_TO_MICRO_MULTIPLIER;
    }
}
