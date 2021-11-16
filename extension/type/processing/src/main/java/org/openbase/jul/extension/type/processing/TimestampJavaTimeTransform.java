package org.openbase.jul.extension.type.processing;

/*-
 * #%L
 * JUL Extension Type Processing
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.openbase.type.timing.TimestampType.Timestamp;
import org.openbase.type.timing.TimestampType.TimestampOrBuilder;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TimestampJavaTimeTransform {

    public static final long FROM_MILLI_TO_MICRO_MULTIPLIER = 1000;

    /**
     * Method transforms the given time in milliseconds into an Timestamp object.
     *
     * @param milliseconds the time in milliseconds.
     * @return a timestamp object representing the given time.
     */
    public static Timestamp transform(final long milliseconds) {
        return Timestamp.newBuilder().setTime(milliseconds * FROM_MILLI_TO_MICRO_MULTIPLIER).build();
    }

    /**
     * Method extracts the milliseconds of the given timestamp.
     *
     * @param timestamp the timestamp to readout the milliseconds.
     * @return the time in milliseconds.
     */
    public static long transform(final TimestampOrBuilder timestamp) {
        return timestamp.getTime() / FROM_MILLI_TO_MICRO_MULTIPLIER;
    }
}
