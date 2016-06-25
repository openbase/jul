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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 *
 * RecurrenceEventFilter helps to filter high frequency events. After a new incoming event is processed, all further incoming events are skipped except of the last event which is executed after the
 * defined timeout is reached.
 * @param <VALUE>
 */
public abstract class RecurrenceEventFilter<VALUE> {

    private static final org.slf4j.Logger logger = LoggerFactory.getLogger(RecurrenceEventFilter.class);

    public static final long DEFAULT_TIMEOUT = 1000;
    public static final long DEFAULT_TEST_TIMEOUT = 100;

    private Timeout timeout;
    private VALUE lastValue;

    private boolean changeDetected;

    public RecurrenceEventFilter() {
        this(DEFAULT_TIMEOUT);
    }

    /**
     * Timeout in milliseconds.
     * @param timeout
     */
    public RecurrenceEventFilter(long timeout) {
        this.changeDetected = false;

        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                timeout = Math.min(timeout, DEFAULT_TEST_TIMEOUT);
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        this.timeout = new Timeout(timeout) {

            @Override
            public void expired() {
                if (changeDetected) {
                    callRelay();
                }
            }
        };
    }

    public synchronized void trigger(final VALUE value) {
        this.lastValue = value;
        trigger();
    }

    public synchronized void trigger() {
        if (timeout.isActive()) {
            changeDetected = true;
            return;
        }

        changeDetected = false;
        callRelay();
        timeout.start();
    }

    public void cancel() {
        timeout.cancel();
    }

    public VALUE getLastValue() {
        return lastValue;
    }

    private void callRelay() {
        try {
            relay();
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
        }
    }

    public abstract void relay() throws Exception;
}
