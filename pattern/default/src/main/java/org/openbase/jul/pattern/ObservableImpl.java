package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <T> the data type on whose changes is notified
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ObservableImpl<S, T> extends AbstractObservable<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservableImpl.class);

    protected final Object VALUE_LOCK = new Object() {
        @Override
        public String toString() {
            return "ObservableValueLock";
        }
    };

    private T value;
    private CompletableFutureLite<T> valueFuture;

    /**
     * {@inheritDoc}
     */
    public ObservableImpl() {
        super();
        this.valueFuture = new CompletableFutureLite<>();
    }

    /**
     * {@inheritDoc}
     *
     * @param source {@inheritDoc}
     */
    public ObservableImpl(final S source) {
        super(source);
        this.valueFuture = new CompletableFutureLite<>();
    }

    /**
     * {@inheritDoc}
     *
     * @param unchangedValueFilter {@inheritDoc}
     */
    public ObservableImpl(final boolean unchangedValueFilter) {
        super(unchangedValueFilter);
        this.valueFuture = new CompletableFutureLite<>();
    }

    /**
     * {@inheritDoc}
     *
     * @param unchangedValueFilter {@inheritDoc}
     * @param source               {@inheritDoc}
     */
    public ObservableImpl(final boolean unchangedValueFilter, final S source) {
        super(unchangedValueFilter, source);
        this.valueFuture = new CompletableFutureLite<>();
    }

    /**
     * {@inheritDoc}
     *
     * @param timeout  {@inheritDoc}
     * @param timeUnit {@inheritDoc}
     * @throws InterruptedException     {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void waitForValue(final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        synchronized (VALUE_LOCK) {
            if (value != null) {
                return;
            }
            // if 0 wait forever like the default java wait() implementation.
            if (timeUnit.toMillis(timeout) == 0) {
                VALUE_LOCK.wait();
            } else {
                timeUnit.timedWait(VALUE_LOCK, timeout);
            }
            if (value == null) {
                throw new NotAvailableException(Observable.class, new TimeoutException());
            }
        }

        //waitUntilNotificationIsFinished();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public T getValue() throws NotAvailableException {
        if (value == null) {
            throw new NotAvailableException("Value");
        }
        return value;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Future<T> getValueFuture() {
        return valueFuture;
    }

    /**
     * {@inheritDoc }
     *
     * @return {@inheritDoc }
     */
    @Override
    public boolean isValueAvailable() {
        return value != null;
    }

    /**
     * {@inheritDoc}
     *
     * @param value {@inheritDoc}
     */
    @Override
    protected void applyValueUpdate(final T value) throws NotAvailableException {

        if(value == null) {
            throw new NotAvailableException("Value");
        }

        synchronized (VALUE_LOCK) {
            this.value = value;
            this.valueFuture.complete(value);
            VALUE_LOCK.notifyAll();
        }
    }
}
