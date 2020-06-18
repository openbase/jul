package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
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

import org.openbase.jul.exception.TimeoutException;

import java.util.concurrent.TimeUnit;

/**
 * Class can be use to split a given timeout between multiple wait or get calls within a method,
 * while it makes sure that the method does not block longer in total then the given timeout.
 * <p>
 * Usage Example:
 * {@code public void waitForABC(final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
 * final TimeoutSplitter timeSplit = new TimeoutSplitter(timeout, timeUnit);
 * <p>
 * waitForA(timeSplit.getTime(), TimeUnit.MILLISECONDS);
 * waitForB(timeSplit.getTime(), TimeUnit.MILLISECONDS);
 * C.wait(timeSplit.getTime());
 * }}
 */
public class TimeoutSplitter {

    public static final TimeUnit DEFAULT_TIME_UNIT = TimeUnit.MILLISECONDS;

    /**
     * In milliseconds
     */
    private final long timestamp;

    /**
     * In milliseconds
     */
    private final long timeout;

    /**
     * @param timeout  the total max time to wait.
     * @param timeUnit the time unit of the given {@code timeout}.
     */
    public TimeoutSplitter(final long timeout, final TimeUnit timeUnit) {
        this.timestamp = System.currentTimeMillis();
        this.timeout = timeUnit.toMillis(timeout);
    }

    /**
     * The time left.
     *
     * @return the time in milliseconds.
     *
     * @throws TimeoutException is thrown when the timeout is reached.
     */
    public long getTime() throws TimeoutException {
        final long time = timeout - (System.currentTimeMillis() - timestamp);
        if (time < 0) {
            throw new TimeoutException();
        }
        return time;
    }

    /**
     * This method returns time unit of the {@code getTime()} method returned value, which is milliseconds by default.
     * For details checkout {@code TimeoutSplitter.DEFAULT_TIME_UNIT}.
     *
     * @return the millisecond time unit.
     */
    public TimeUnit getTimeUnit() {
        return DEFAULT_TIME_UNIT;
    }

    /**
     * Returns the timestamp of when this object was created.
     * @return the timestamp in ms.
     */
    public long getTimestamp() {
        return timestamp;
    }
}
