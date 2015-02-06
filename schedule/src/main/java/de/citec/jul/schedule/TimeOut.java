/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 *
 */
public abstract class TimeOut {

    private static final Logger logger = LoggerFactory.getLogger(TimeOut.class);
    
    private final Object lock = new Object();
    private Thread timerThread;
    private long waitTime;

    public TimeOut(final long waitTime) {
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
//						logger.info("wait");
                        Thread.sleep(waitTime);
                    } catch (InterruptedException ex) {
//                        Logger.debug(this, "TimeOut interrupted.");
                        return;
                    }
                    try {
						logger.debug("Expire...");
                        expired();
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
