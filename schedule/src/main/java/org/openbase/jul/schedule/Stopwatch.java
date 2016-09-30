package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * This class implements a general purpose stopwatch.
 *
 * This implementation should be thread safe.
 */
public class Stopwatch {

    private final SyncObject startWaiter = new SyncObject("StartWaiter");
    private final SyncObject stopWaiter = new SyncObject("StopWaiter");
    private final SyncObject timeSync = new SyncObject("TimeSync");

    private long startTime, endTime;

    /**
     * Constructor initializes a new Stopwatch.
     */
    public Stopwatch() {
        reset();
    }

    /**
     * This method resets the Stopwatch by clearing the internally stored start and end timestamps.
     */
    public final void reset() {
        synchronized (timeSync) {
            this.startTime = -1;
            this.endTime = -1;
        }
    }

    /**
     * This method starts the Stopwatch by setting the start timestamp.
     */
    public void start() {
        synchronized (startWaiter) {
            synchronized (timeSync) {
                startTime = System.currentTimeMillis();
            }
            startWaiter.notifyAll();
        }
    }

    /**
     * This method restarts the Stopwatch.
     *
     * Internally the reset() and start() methods are called.
     */
    public void restart() {
        synchronized (timeSync) {
            reset();
            start();
        }
    }

    /**
     * This method returns the time interval between the start- and end timestamps.
     * In case the the Stopwatch is still running, the elapsed time since Stopwatch start will be returned.
     *
     * @return the time interval in milliseconds.
     * @throws NotAvailableException This exception will thrown in case the timer was never started.
     */
    public long getTime() throws NotAvailableException {
        synchronized (timeSync) {
            try {
                if (!isRunning()) {
                    throw new InvalidStateException("Stopwatch was never started!");
                }

                if (endTime == -1) {
                    return System.currentTimeMillis() - startTime;
                }

                return endTime - startTime;
            } catch (CouldNotPerformException ex) {
                throw new NotAvailableException("time", ex);
            }
        }
    }

    /**
     * Method checks if the Stopwatch was started after reset.
     *
     * Even after calling the stop method the Stopwatch is still running until reset() is called.
     *
     * @return true if the Stopwatch was started.
     */
    public boolean isRunning() {
        synchronized (timeSync) {
            return startTime != -1;
        }
    }

    /**
     * This method stops the Stopwatch and returns the time result.
     *
     * The internal timestamps are not cleared by this method so the result can still queried by the getTime() method afterwards.
     * Additionally this method can be called multiply times but only the last stop timestamp will be stored and handled by the getTime() method.
     *
     * @return the elapsed time interval since start.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the Stopwatch was never started.
     */
    public long stop() throws CouldNotPerformException {
        synchronized (timeSync) {
            if (!isRunning()) {
                throw new InvalidStateException("Stopwatch was never started!");
            }

            synchronized (stopWaiter) {
                endTime = System.currentTimeMillis();
                stopWaiter.notifyAll();
            }

            return getTime();
        }
    }

    /**
     * Method returns the timestamp of the Stopwatch start.
     *
     * @return the timestamp in milisecunds.
     * @throws org.openbase.jul.exception.NotAvailableException is thrown in case the Stopwatch was never started.
     */
    public long getStartTime() throws NotAvailableException {
        if (startTime == -1) {
            throw new NotAvailableException("StartTime");
        }
        return startTime;
    }

    /**
     * Method returns the timestamp of the last Stopwatch stop.
     *
     * @return the timestamp in milisecunds.
     * @throws org.openbase.jul.exception.NotAvailableException is thrown in case the Stopwatch was never started.
     */
    public long getEndTime() throws NotAvailableException {
        if (endTime == -1) {
            throw new NotAvailableException("StopTime");
        }
        return endTime;
    }

    /**
     * Method blocks until the Stopwatch start or the given timeout expired.
     *
     * @param timeout max time to wait.
     * @throws InterruptedException is thrown in case the current thread was externally interruped.
     */
    public void waitForStart(final long timeout) throws InterruptedException {
        synchronized (startWaiter) {
            if (isRunning()) {
                return;
            }
            startWaiter.wait(timeout);
        }
    }

    /**
     * Method blocks until the Stopwatch start.
     *
     * @throws InterruptedException is thrown in case the current thread was externally interruped.
     */
    public void waitForStart() throws InterruptedException {
        synchronized (startWaiter) {
            if (isRunning()) {
                return;
            }
            startWaiter.wait();
        }
    }

    /**
     * Method blocks until the Stopwatch stop or the given timeout expired.
     *
     * @param timeout max time to wait.
     * @throws InterruptedException is thrown in case the current thread was externally interruped.
     */
    public void waitForStop(final long timeout) throws InterruptedException {
        synchronized (stopWaiter) {
            if (endTime != -1) {
                return;
            }
            stopWaiter.wait(timeout);
        }
    }

    /**
     * Method blocks until the Stopwatch stop.
     *
     * @throws InterruptedException is thrown in case the current thread was externally interruped.
     */
    public void waitForStop() throws InterruptedException {
        synchronized (stopWaiter) {
            if (endTime != -1) {
                return;
            }
            stopWaiter.wait();
        }
    }
}
