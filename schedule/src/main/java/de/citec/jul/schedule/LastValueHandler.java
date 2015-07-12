/*

 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 * @param <V> The value to handle.
 */
public abstract class LastValueHandler<V> implements Runnable {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(LastValueHandler.class);

    public final static long DEFAULT_DELAY = 0;

    private final String name;
    private V value;
    private V oldValue;
    private final Object exetcuterWaiter = new Object();
    private final Object EXECUTER_CONTROL_LOCK = new Object();
    private Thread executer;

    private long delayUntilNext;

    public LastValueHandler(String name) {
        this(name, DEFAULT_DELAY);
    }

    public LastValueHandler(final String name, final long delayUntilNext) {
        this.name = name;
        this.delayUntilNext = delayUntilNext;
        this.value = null;
        this.oldValue = null;
        this.executer = null;
    }

    @Override
    public void run() {
//		Logger.info(LastValueHandler.this, "Run...");
        try {
            while (!executer.isInterrupted()) {
//				Logger.info(LastValueHandler.this, "Run next");
                while (hasValueChanged()) {
                    handle(value);
                }

                if (delayUntilNext != 0) {
                    Thread.sleep(delayUntilNext);
                }

                try {
                    synchronized (exetcuterWaiter) {
                        Thread.yield();
                        exetcuterWaiter.wait();
                    }
                } catch (InterruptedException ex) {
//					Logger.info(LastValueHandler.this, "Run interupted");
                    break;
                }
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Handler thread crashed!", ex));
        }
//		Logger.info(LastValueHandler.this, "Run finished");
//		Logger.info(LastValueHandler.this, "Run finished");
    }

    private boolean hasValueChanged() {
        if (value != oldValue) {
            oldValue = this.value;
            return true;
        }
        return false;
    }

    /**
     * Configure the delay between to executions if the value changed during
     * processing.
     *
     * @param delay
     */
    public void setDelay(final long delay) {
        delayUntilNext = delay;
    }

    public void start() throws InterruptedException {
        synchronized (EXECUTER_CONTROL_LOCK) {
//		Logger.info(this, "Start...");
            if (executer != null) {
                if (executer.isAlive()) {
//				Logger.info(this, "Start already running!");
                    return;
                }
                if (executer.isInterrupted()) {
                    executer.join();
                }
                executer = null;
            }
            executer = new Thread(this, name);
            executer.start();
        }
//		Logger.info(this, "Start finished.");
    }

    public void stop() throws InterruptedException {
        synchronized (EXECUTER_CONTROL_LOCK) {
            // Logger.info(this, "Stop...");
            if (executer == null) {
                //Logger.info(this, "Stop already stoped.");
                return;
            }

            // Logger.info(this, "Stop interrupt.");
            executer.interrupt();
            executer.join();
            // Logger.info(this, "Stop finished.");
        }
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        if (this.value == value) {
            return;
        }

        this.value = value;
        synchronized (exetcuterWaiter) {
            exetcuterWaiter.notify();
        }
    }

    public abstract void handle(V value);
}
