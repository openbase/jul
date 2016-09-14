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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
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
    private boolean valueChanged;

    private long delayUntilNext;

    public LastValueHandler(String name) {
        this(name, DEFAULT_DELAY);
    }

    public LastValueHandler(final String name, final long delayUntilNext) {
        this.name = name;
        this.delayUntilNext = delayUntilNext;
        this.value = null;
        this.valueChanged = false;
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
            ExceptionPrinter.printHistory(new CouldNotPerformException("Handler thread crashed!", ex), logger);
        }
//		Logger.info(LastValueHandler.this, "Run finished");
//		Logger.info(LastValueHandler.this, "Run finished");
    }

    private boolean hasValueChanged() {
        if (value != oldValue) {
            oldValue = this.value;
            return true;
        }
        
        if(valueChanged) {
            valueChanged = false;
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
    
    public void forceValueChange() {
        valueChanged = true;
        synchronized (exetcuterWaiter) {
            exetcuterWaiter.notify();
        }
    }

    public abstract void handle(V value);
}
