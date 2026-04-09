package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.ExceptionProcessor;
import org.openbase.jul.exception.ShutdownInProgressException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.TimedProcessable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class Timeout {

    /**
     * Using Long.MAX_VALUE as infinity timeout is not practical because in any calculations using this timeout like adding +1 causes a value overrun.
     * Therefore, this constant is introduced to use a infinity timeout which represents in fact 3170 years which should covers at least some human generations ;)
     * <p>
     * The unit of the {@code INFINITY_TIMEOUT} is in milliseconds.
     */
    public static final long INFINITY_TIMEOUT = TimedProcessable.INFINITY_TIMEOUT;

    private static final Logger logger = LoggerFactory.getLogger(Timeout.class);

    private final Object lock = new SyncObject("TimeoutLock");
    private Future timerTask;

    /**
     * The default time to wait in milliseconds.
     */
    private long defaultWaitTime;
    private volatile boolean expired;
    private long startTimestamp;
    private long timeToWait;

    /**
     * Constructor creates a new Timeout instance. The default timeout can be configured via the given {@code defaultWaitTime} argument.
     *
     * @param defaultWaitTime the default timeout in millisecond.
     * @param timeUnit        the time unit of the {@code defaultWaitTime}.
     */
    public Timeout(final long defaultWaitTime, final TimeUnit timeUnit) {
        this(timeUnit.toMillis(defaultWaitTime));
    }

    /**
     * Constructor creates a new Timeout instance. The default timeout can be configured via the given {@code defaultWaitTime} argument.
     *
     * @param defaultWaitTime the default timeout in millisecond.
     */
    public Timeout(final long defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
    }

    /**
     * Constructor creates a new Timeout instance. The default timeout can be configured via the given {@code defaultWaitTime} argument.
     *
     * @param defaultWaitTime the default timeout.
     */
    public Timeout(final Duration defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime.toMillis();
    }

    /**
     * Returns the currently configured time to wait until the timeout is reached after start.
     *
     * @return the time in milliseconds.
     */
    public long getTimeToWait() {
        return timeToWait;
    }

    /**
     * Returns the currently configured default time to wait until the timeout is reached after start.
     *
     * @return the time in milliseconds.
     */
    public long getDefaultWaitTime() {
        return defaultWaitTime;
    }

    /**
     * Method restarts the timeout.
     *
     * @param waitTime the new wait time to update in milliseconds.
     *
     * @throws CouldNotPerformException    is thrown in case the timeout could not be restarted.
     * @throws ShutdownInProgressException is thrown in case the the system is currently shutting down.
     */
    public void restart(final long waitTime) throws CouldNotPerformException {
        restart(waitTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Method restarts the timeout.
     *
     * @param waitTime the new wait time to update.
     * @param timeUnit the time unit of the wait time.
     *
     * @throws CouldNotPerformException    is thrown in case the timeout could not be restarted.
     * @throws ShutdownInProgressException is thrown in case the the system is currently shutting down.
     */
    public void restart(final long waitTime, final TimeUnit timeUnit) throws CouldNotPerformException {
        //logger.trace("Reset timer.");
        try {
            synchronized (lock) {
                cancel();
                start(waitTime, timeUnit);
            }
        } catch (ShutdownInProgressException ex) {
            throw ex;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not restart timer!", ex);
        }
    }

    /**
     * Method restarts the timeout.
     *
     * @throws CouldNotPerformException    is thrown in case the timeout could not be restarted.
     * @throws ShutdownInProgressException is thrown in case the the system is currently shutting down.
     */
    public void restart() throws CouldNotPerformException {
        restart(defaultWaitTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Return true if the given timeout is expired.
     *
     * @return
     */
    public boolean isExpired() {
        return expired;
    }

    /**
     * Return true if the timeout is still running or the expire routine is still executing.
     *
     * @return
     */
    public boolean isActive() {
        synchronized (lock) {
            return timerTask != null && !timerTask.isDone();
        }
    }

    /**
     * Start the timeout with the default wait time.
     *
     * @throws CouldNotPerformException    is thrown in case the timeout could not be started.
     * @throws ShutdownInProgressException is thrown in case the the system is currently shutting down.
     */
    public void start() throws CouldNotPerformException {
        start(defaultWaitTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Start the timeout with the given wait time. The default wait time is not modified and still the same as before.
     *
     * @param waitTime the time to wait until the timeout is reached in milliseconds.
     *
     * @throws CouldNotPerformException    is thrown in case the timeout could not be started.
     * @throws ShutdownInProgressException is thrown in case the the system is currently shutting down.
     */
    public void start(final long waitTime) throws CouldNotPerformException {
        start(waitTime, TimeUnit.MILLISECONDS);
    }

    /**
     * Start the timeout with the given wait time. The default wait time is not modified and still the same as before.
     *
     * @param waitTime the time to wait until the timeout is reached.
     * @param timeUnit the time unit of the wait time.
     *
     * @throws CouldNotPerformException    is thrown in case the timeout could not be started.
     * @throws ShutdownInProgressException is thrown in case the the system is currently shutting down.
     */
    public void start(final long waitTime, final TimeUnit timeUnit) throws CouldNotPerformException {
        try {
            internal_start(waitTime, timeUnit);
        } catch (CouldNotPerformException | RejectedExecutionException ex) {
            if (ex instanceof RejectedExecutionException && GlobalScheduledExecutorService.getInstance().getExecutorService().isShutdown()) {
                throw new ShutdownInProgressException("GlobalScheduledExecutorService");
            }
            throw new CouldNotPerformException("Could not start " + this, ex);
        }
    }

    /**
     * Internal synchronized start method.
     *
     * @param waitTime The time to wait until the timeout is reached.
     * @param timeUnit the time unit of the wait time.
     *
     * @throws RejectedExecutionException is thrown if the timeout task could not be scheduled.
     * @throws CouldNotPerformException   is thrown in case the timeout could not be started.
     */
    private void internal_start(final long waitTime, final TimeUnit timeUnit) throws RejectedExecutionException, CouldNotPerformException {
        synchronized (lock) {
            if (isActive()) {
                logger.debug("Reject start, not interrupted or expired.");
                return;
            }
            expired = false;
            startTimestamp = System.currentTimeMillis();
            timeToWait = waitTime;
            timerTask = GlobalScheduledExecutorService.schedule((Callable<Void>) () -> {
                synchronized (lock) {
                    try {
                        //logger.trace("Wait for timeout TimeOut interrupted.");
                        if (timerTask.isCancelled()) {
                            logger.trace("TimeOut was canceled.");
                            return null;
                        }
                        //logger.trace("Expire...");
                        expired = true;
                    } finally {
                        timerTask = null;
                    }
                }

                try {
                    expired();
                } catch (InterruptedException ex) {
                    // just finish task on interruption
                } catch (Exception ex) {
                    if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Error during timeout handling!", ex), logger, LogLevel.WARN);
                    }
                }
                //logger.trace("Worker finished.");
                return null;
            }, waitTime, timeUnit);
        }
    }

    /**
     * Method cancels the a started timeout.
     * <p>
     * Node: In case the timeout was never started this method does nothing.
     */
    public void cancel() {
        //logger.trace("try to cancel timer.");
        synchronized (lock) {
            if (timerTask != null) {
                //logger.trace("cancel timer.");
                timerTask.cancel(false);
            }
        }
    }

    /**
     * @param waitTime
     *
     * @deprecated since v2.0 and will be removed in v3.0. Please  use setDefaultWaitTime instead.
     */
    @Deprecated
    public void setWaitTime(long waitTime) {
        setDefaultWaitTime(waitTime);
    }

    /**
     * Method setup the default time to wait until the timeout is reached.
     *
     * @param waitTime the time to wait in milliseconds.
     */
    public void setDefaultWaitTime(long waitTime) {
        this.defaultWaitTime = waitTime;
    }

    /**
     * This method is called in case a timeout is reached.
     * <p>
     * This method should be overwritten by a timeout implementation.
     *
     * @throws InterruptedException
     */
    public abstract void expired() throws InterruptedException;

    /**
     * Prints a human readable representation of this timeout.
     *
     * @return a timeout description as string.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[wait:" + defaultWaitTime + "]";
    }

    /**
     * Returns the time left until the timeout will expire.
     *
     * @return time in milliseconds.
     */
    public long getTimeLeftUntilTimeout() {
        return startTimestamp + timeToWait - System.currentTimeMillis();
    }

    /**
     * Returns the time passed since this timer was started.
     *
     * @return time in milliseconds.
     */
    public long getTimePassedSinceStart() {
        return System.currentTimeMillis() - startTimestamp;
    }

    /**
     * Method returns an huge timeout which will never expire
     * Using Long.MAX_VALUE as infinity timeout is not practical because in any calculations using this timeout like adding +1 causes a value overrun.
     * Therefore, this method delivers a infinity timeout which represents in fact 3170 years which should covers at least some human generations ;)
     *
     * @param timeUnit the time unit used for the timeout to return.
     *
     * @return the timeout.
     */
    public static long getInfinityTimeout(final TimeUnit timeUnit) {
        return timeUnit.convert(INFINITY_TIMEOUT, TimeUnit.MILLISECONDS);
    }
}
