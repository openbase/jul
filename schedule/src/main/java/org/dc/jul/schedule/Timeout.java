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

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 *
 */
public abstract class Timeout {

    private static final Logger logger = LoggerFactory.getLogger(Timeout.class);

    private final ExecutorService executorService;
    private final Object lock = new Object();
    private Thread timerThread;
    private long waitTime;
    private boolean expired;

    public Timeout(final long waitTime) {
        this.waitTime = waitTime;
        this.executorService = Executors.newCachedThreadPool();
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
            return timerThread != null && timerThread.isAlive();
        }
    }

    public void start(final long waitTime) {
        this.waitTime = waitTime;
        start();
    }

    public void start() {
        synchronized (lock) {
            if (timerThread != null && !timerThread.isInterrupted() && timerThread.isAlive()) {
                logger.debug("Cancel start, not interupted or expired.");
                return;
            }

            logger.debug("Create new timer");
            timerThread = new Thread(getClass().getSimpleName() + "[wait:" + waitTime + "]") {

                @Override
                public void run() {
                    try {
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
                        logger.debug("TimeOut interrupted.");
                        return;
                    }
                    try {
                        logger.debug("Expire...");
                        expired = true;
                        //TODO mpohling: good idea but depending unit tests are failing. Please check!
//                        executorService.submit(() -> {
                            expired();
//                        });

                    } catch (Exception ex) {
                        logger.debug("Error during timeout handling!", ex);
                    }
                    logger.debug("Worker finished.");
                }
            };
            timerThread.start();
        }
    }

    public void cancel() {
        synchronized (lock) {
            if (timerThread != null) {
                logger.debug("interrupt timer.");
                timerThread.interrupt();
                timerThread = null;
            }
        }
    }

    public abstract void expired();

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[wait:" + waitTime + "]";
    }
}
