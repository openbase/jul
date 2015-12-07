/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

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
