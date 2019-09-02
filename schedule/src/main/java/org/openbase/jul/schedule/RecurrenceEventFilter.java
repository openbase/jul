package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.ShutdownInProgressException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;

/**
 * @param <VALUE>
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * <p>
 * RecurrenceEventFilter helps to filter high frequency events.
 * After a new incoming event is processed, all further incoming events are skipped except of the last event which is executed after the defined timeout is reached.
 */
public abstract class RecurrenceEventFilter<VALUE> {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RecurrenceEventFilter.class);

    public static final long DEFAULT_MAX_FREQUENCY = 1000;
    public static final long TEST_MAX_FREQUENCY = 10;

    private Timeout timeout;
    private VALUE latestValue;
    private boolean triggered;

    private boolean changeDetected;

    /**
     * Constructor creates a new {@code RecurrenceEventFilter} instance pre-configured with the given {@code DEFAULT_MAX_FREQUENCY}.
     */
    public RecurrenceEventFilter() {
        this(DEFAULT_MAX_FREQUENCY);
    }

    /**
     * Constructor creates a new {@code RecurrenceEventFilter} instance pre-configured with the given {@code maxFrequency}.
     *
     * @param maxFrequency this is the maximum frequency in milliseconds where triggered events are relayed.
     */
    public RecurrenceEventFilter(long maxFrequency) {
        this.changeDetected = false;
        this.triggered = false;
        this.latestValue = null;

        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                maxFrequency = Math.min(maxFrequency, TEST_MAX_FREQUENCY);
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), LOGGER);
        }

        this.timeout = new Timeout(maxFrequency) {

            @Override
            public void expired() {
                if (changeDetected) {
                    callRelay();
                }
            }
        };
    }

    /**
     * This method triggers the relay if no trigger was relayed within the defined max frequency.
     * <p>
     * Note: Triggers are maybe filtered but the last trigger call will always result in a relay to guarantee the latest event will relayed.
     *
     * @throws CouldNotPerformException is thrown if the trigger could not be handled (e.g. because of a system shutdown).
     */
    public synchronized void trigger() throws CouldNotPerformException {
        trigger(latestValue, false);
    }

    /**
     * This method triggers the relay if no trigger was relayed within the defined max frequency.
     * <p>
     * Note: Triggers are maybe filtered but the last trigger call will always result in a relay to guarantee the latest value will relayed.
     *
     * @param value the new value which should be published via the next relay.
     *
     * @throws CouldNotPerformException is thrown if the trigger could not be handled (e.g. because of a system shutdown).
     */
    public synchronized void trigger(final VALUE value) throws CouldNotPerformException {
        trigger(value, false);
    }

    /**
     * This method triggers the relay if no trigger was relayed within the defined max frequency or the {@code immediately} flag was set.
     * <p>
     * Note: Triggers are maybe filtered but the last trigger call will always result in a relay to guarantee the latest event will relayed.
     *
     * @param immediately this flag forces the trigger to relay immediately without respect to the defined max frequency.
     *
     * @throws CouldNotPerformException is thrown if the trigger could not be handled (e.g. because of a system shutdown).
     */
    public synchronized void trigger(final boolean immediately) throws CouldNotPerformException {
        trigger(latestValue, immediately);
    }

    /**
     * This method triggers the relay if no trigger was relayed within the defined max frequency or the {@code immediately} flag was set.
     * <p>
     * Note: Triggers are maybe filtered but the last trigger call will always result in a relay to guarantee the latest value will relayed.
     *
     * @param value       the new value which should be published via the next relay.
     * @param immediately this flag forces the trigger to relay immediately without respect to the defined max frequency.
     *
     * @throws CouldNotPerformException is thrown if the trigger could not be handled (e.g. because of a system shutdown).
     */
    public synchronized void trigger(final VALUE value, final boolean immediately) throws CouldNotPerformException {
        latestValue = value;
        triggered = true;
        try {
            if (timeout.isActive()) {
                if (!immediately) {
                    changeDetected = true;
                    return;
                }
                timeout.cancel();
            }

            changeDetected = false;
            callRelay();
            timeout.start();
        } catch (final ShutdownInProgressException ex) {
            // just skip trigger when shutdown is in progress
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not trigger " + this, ex);
        }
    }

    /**
     * Method cancels depending triggers which were not relayed yet.
     */
    public void cancel() {
        timeout.cancel();
    }

    /**
     * Method returns the latest triggered value.
     *
     * @return the last value or null if no last value is available
     *
     * @deprecated since v2.0 and will be removed in v3.0. Please use {@code getLatestValue()} instead.
     */
    @Deprecated
    public VALUE getLastValue() {
        try {
            return getLatestValue();
        } catch (NotAvailableException ex) {
            return null;
        }
    }

    /**
     * Method returns the latest triggered value.
     *
     * @return the latest value.
     *
     * @throws NotAvailableException is thrown if a value was never triggered.
     */
    public VALUE getLatestValue() throws NotAvailableException {
        if (latestValue == null) {
            throw new NotAvailableException("LatestValue");
        }
        return latestValue;
    }

    /**
     * Method checks if there was an event registered within the last filter period and the filter is therefore active.
     *
     * @return true if any new incoming event would be queued, otherwise false.
     */
    public boolean isFilterActive() {
        return timeout.isActive();
    }


    /**
     * Method returns if this instance was ever triggered since startup or since the last reset.
     *
     * @return
     */
    public boolean isTriggered() {
        return triggered;
    }

    /**
     * Method cancels all depending triggers and resets the {@code triggered} flag as well as resets the {@code lastValue} to {@code null}.
     */
    public void reset() {
        cancel();
        latestValue = null;
        triggered = false;
        changeDetected = true;
    }

    private void callRelay() {
        try {
            relay(latestValue);
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(ex, LOGGER, LogLevel.ERROR);
        }
    }

    /**
     * Method can be overwritten to get frequently informed about trigger actions and the related new value.
     * <p>
     * Note: Be informed that by overwriting this method the default {@code relay()} will not be called anymore.
     *
     * @param value the latest value is passed via this argument.
     *
     * @throws Exception can be thrown during the relay. The exception will just be printed on the error channel.
     */
    public void relay(final VALUE value) throws Exception {
        relay();
    }

    /**
     * Method should be overwritten to get frequently informed about trigger actions.
     *
     * @throws Exception can be thrown during the relay. The exception will just be printed on the error channel.
     */
    public abstract void relay() throws Exception;
}
