package org.dc.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
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
        logger.info("Reset timer.");
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
                logger.info("Reject start, not interrupted or expired.");
                return;
            }
            expired = false;

            logger.info("Create new timer");
            // TODO may a global scheduled executor service is more suitable.
            timerTask = GlobalExecuterService.submit(new Callable<Void>() {

                @Override
                public Void call() throws InterruptedException {
                    logger.info("Wait for timeout TimeOut interrupted.");
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        logger.info("TimeOut interrupted.");
                        throw ex;
                    }
                    try {
                        logger.info("Expire...");
                        expired = true;
                        expired();
                    } catch (Exception ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Error during timeout handling!", ex), logger, LogLevel.WARN);
                    }
                    logger.info("Worker finished.");
                    return null;
                }
            });
        }
    }

    public void cancel() {
        logger.info("try to cancel timer.");
        synchronized (lock) {
            if (timerTask != null) {
                logger.info("cancel timer.");
                timerTask.cancel(true);
                timerTask = null;
            } else {
                logger.warn("timer was canceled but never started!");
            }
        }
    }

    public abstract void expired();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[wait:" + waitTime + "]";
    }
}
