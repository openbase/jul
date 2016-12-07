package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * $Id:$
 * $HeadURL:$
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
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 */
public abstract class Timeout {

    private static final Logger logger = LoggerFactory.getLogger(Timeout.class);

    private final Object lock = new SyncObject("TimeoutLock");
    private final Object cancelLock = new SyncObject("TimoutCanelLock");
    private Future timerTask;
    private long defaultWaitTime;
    private boolean expired;

    public Timeout(final long defaultWaitTime) {
        this.defaultWaitTime = defaultWaitTime;
    }

    public long getTimeToWait() {
        return defaultWaitTime;
    }

    public void restart(final long waitTime) {
        logger.debug("Reset timer.");
        synchronized (lock) {
            cancel();
            start(waitTime);
        }
    }

    public void restart() {
        restart(defaultWaitTime);
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

    public void start() {
        internal_start(defaultWaitTime);
    }

    public void start(final long waitTime) {
        internal_start(waitTime);
    }

    private void internal_start(final long waitTime) {
        synchronized (lock) {
            if (timerTask != null && !timerTask.isCancelled() && !timerTask.isDone()) {
                logger.debug("Reject start, not interrupted or expired.");
                return;
            }
            this.defaultWaitTime = waitTime;
            expired = false;

//            logger.info("Create new timer");
            // TODO may a global scheduled executor service is more suitable.
            timerTask = GlobalExecutionService.submit(new Callable<Void>() {

                @Override
                public Void call() throws InterruptedException {
                    try {
                        logger.debug("Wait for timeout TimeOut interrupted.");
                        try {
                            synchronized (cancelLock) {
                                cancelLock.wait(waitTime);
                                if (timerTask.isCancelled()) {
                                    logger.debug("TimeOut was canceled.");
                                    return null;
                                }
                            }
                        } catch (InterruptedException ex) {
                            logger.debug("TimeOut was interrupted.");
                            return null;
                        }
                        logger.debug("Expire...");
                        expired = true;
                    } finally {
                        synchronized (lock) {
                            timerTask = null;
                        }
                    }

                    try {
                        expired();
                    } catch (Exception ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Error during timeout handling!", ex), logger, LogLevel.WARN);
                    }
                    logger.debug("Worker finished.");
                    return null;
                }
            });
        }
    }

    public void cancel() {
        logger.debug("try to cancel timer.");
        synchronized (lock) {
            if (timerTask != null) {
                logger.debug("cancel timer.");
                synchronized (cancelLock) {
                    timerTask.cancel(false);
                    cancelLock.notifyAll();
                }
            } else {
                logger.debug("timer was canceled but never started!");
            }
        }
    }

    public void setWaitTime(long waitTime) {
        this.defaultWaitTime = waitTime;
    }

    public abstract void expired() throws InterruptedException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[wait:" + defaultWaitTime + "]";
    }
}
