package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern
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
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <T> the data type on whose changes is notified
 */
public class ObservableImpl<T> implements Observable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservableImpl.class);

    private static final boolean DEFAULT_UNCHANGED_VALUE_FILTER = true;

    private final boolean unchangedValueFilter;
    private final Object LOCK = new Object();
    private final List<Observer<T>> observers;
    private T value;
    private int latestValueHash;

    /**
     * {@inheritDoc}
     */
    public ObservableImpl() {
        this(DEFAULT_UNCHANGED_VALUE_FILTER);
    }

    /**
     * {@inheritDoc}
     *
     * @param unchangedValueFilter
     */
    public ObservableImpl(final boolean unchangedValueFilter) {
        this.observers = new ArrayList<>();
        this.unchangedValueFilter = unchangedValueFilter;
    }

    /**
     * {@inheritDoc}
     *
     * @param observer
     */
    @Override
    public void addObserver(Observer<T> observer) {
        synchronized (LOCK) {
            if (observers.contains(observer)) {
                LOGGER.warn("Skip observer registration. Observer[" + observer + "] is already registered!");
                return;
            }
            observers.add(observer);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param observer
     */
    @Override
    public void removeObserver(Observer<T> observer) {
        synchronized (LOCK) {
            observers.remove(observer);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        synchronized (LOCK) {
            observers.clear();
        }
    }

    /**
     *
     * @param timeout {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public void waitForValue(final long timeout, final TimeUnit timeUnit) throws NotAvailableException, InterruptedException {
        synchronized (LOCK) {
            if (value != null) {
                return;
            }
            // if 0 wait forever like the default java wait() implementation.
            if (timeUnit.toMillis(timeout) == 0) {
                LOCK.wait();
            } else {
                timeUnit.timedWait(LOCK, timeout);
            }
            if (value == null) {
                throw new NotAvailableException("Observable was not available in time.", new TimeoutException());
            }
        }
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
     * Notify all changes of the observable to all observers only if the observable has changed.
     * The source of the notification is set as this.
     * Because of data encapsulation reasons this method is not included within the Observer interface.
     *
     * @param observable the value which is notified
     * @return true if the observable has changed
     * @throws MultiException thrown if the notification to at least one observer fails
     */
    public boolean notifyObservers(T observable) throws MultiException {
        return notifyObservers(this, observable);
    }

    /**
     * Notify all changes of the observable to all observers only if the observable has changed.
     * Because of data encapsulation reasons this method is not included within the Observer interface.
     *
     * @param source the source of the notification
     * @param observable the value which is notified
     * @return true if the observable has changed
     * @throws MultiException thrown if the notification to at least one observer fails
     */
    public boolean notifyObservers(Observable<T> source, T observable) throws MultiException {
        ExceptionStack exceptionStack = null;

        synchronized (LOCK) {
            if (observable == null) {
                LOGGER.debug("Skip notification because observable is null!");
                return false;
            }
            if (unchangedValueFilter && value != null && observable.hashCode() == latestValueHash) {
                LOGGER.debug("#+# Skip notification because observable has not been changed!");
                return false;
            }

            value = observable;
            latestValueHash = value.hashCode();

            for (Observer<T> observer : new ArrayList<>(observers)) {
                try {
                    observer.update(source, observable);
                } catch (Exception ex) {
                    exceptionStack = MultiException.push(observer, ex, exceptionStack);
                }
            }

            LOCK.notifyAll();
        }
        MultiException.checkAndThrow("Could not notify Data[" + observable + "] to all observer!", exceptionStack);
        return true;
    }
}
