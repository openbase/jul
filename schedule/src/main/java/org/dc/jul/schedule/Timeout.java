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
import java.util.concurrent.Executors;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
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
    private ForkJoinTask timerTask;
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
        cancel();
        start();
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

    public void start(final long waitTime) {
        this.waitTime = waitTime;
        start();
    }

    public void start() {
        synchronized (lock) {
            if (timerTask != null && !timerTask.isCancelled() && !timerTask.isDone()) {
                logger.debug("Reject start, not interupted or expired.");
                return;
            }

            logger.debug("Create new timer");
            timerTask = ForkJoinPool.commonPool().submit(new Callable<Void>() {

                @Override
                public Void call() {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        logger.debug("TimeOut interrupted.");
                        return null;
                    }
                    try {
                        logger.debug("Expire...");
                        expired = true;
                        expired();
                    } catch (Exception ex) {
                        logger.debug("Error during timeout handling!", ex);
                    }
                    logger.debug("Worker finished.");
                    return null;
                }
            });
        }
    }

    public void cancel() {
        synchronized (lock) {
            if (timerTask != null) {
                logger.debug("cancel timer.");
                timerTask.cancel(true);
                timerTask = null;
            }
        }
    }

    public abstract void expired();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[wait:" + waitTime + "]";
    }
}
