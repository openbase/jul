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

    private final Object lock = new Object();
    private Future timerTask;
    private long waitTime;
    private boolean expired;

    public Timeout(final long waitTime) {
        this.waitTime = waitTime;
    }

    public long getTimeToWait() {
        return waitTime;
    }

    public void restart(final long waitTime) {
        this.waitTime = waitTime;
        restart();
    }

    public void restart() {
        logger.debug("Reset timer.");
        synchronized (lock) {
            cancel();
            start();
        }
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
        internal_start(waitTime);
    }

    public void start(final long waitTime) {
        this.waitTime = waitTime;
        internal_start(waitTime);
    }

    private void internal_start(final long waitTime) {
        synchronized (lock) {
            if (timerTask != null && !timerTask.isCancelled() && !timerTask.isDone()) {
                logger.debug("Reject start, not interrupted or expired.");
                return;
            }
            expired = false;

//            logger.info("Create new timer");
            // TODO may a global scheduled executor service is more suitable.
            timerTask = GlobalExecutionService.submit(new Callable<Void>() {

                @Override
                public Void call() throws InterruptedException {
                    logger.debug("Wait for timeout TimeOut interrupted.");
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        logger.debug("TimeOut interrupted.");
                        throw ex;
                    }
                    try {
                        logger.debug("Expire...");
                        expired = true;
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
                timerTask.cancel(true);
                timerTask = null;
            } else {
                logger.debug("timer was canceled but never started!");
            }
        }
    }
    
    public void setWaitTime(long waitTime) {
        this.waitTime = waitTime;
    }

    public abstract void expired() throws InterruptedException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[wait:" + waitTime + "]";
    }
}
