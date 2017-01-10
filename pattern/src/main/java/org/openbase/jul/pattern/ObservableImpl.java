package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.concurrent.TimeUnit;
import org.openbase.jul.exception.CouldNotPerformException;
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
public class ObservableImpl<T> extends AbstractObservable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ObservableImpl.class);

    private T value;

    /**
     * {@inheritDoc}
     */
    public ObservableImpl() {
        super();
    }

    /**
     *
     * {@inheritDoc}
     *
     * @param source {@inheritDoc}
     */
    public ObservableImpl(final Object source) {
        super(source);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @param unchangedValueFilter {@inheritDoc}
     */
    public ObservableImpl(final boolean unchangedValueFilter) {
        super(unchangedValueFilter);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @param unchangedValueFilter {@inheritDoc}
     * @param source {@inheritDoc}
     */
    public ObservableImpl(final boolean unchangedValueFilter, final Object source) {
        super(unchangedValueFilter, source);
    }

    /**
     *
     * {@inheritDoc}
     *
     * @param timeout {@inheritDoc}
     * @param timeUnit {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void waitForValue(final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
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
     * {@inheritDoc }
     *
     * @return {@inheritDoc }
     */
    @Override
    public boolean isValueAvailable() {
        return value != null;
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
    @Override
    public boolean notifyObservers(Observable<T> source, T observable) throws MultiException {
        ExceptionStack exceptionStack = null;

        synchronized (LOCK) {
            try {
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
            } finally {
                LOCK.notifyAll();
            }
        }
        MultiException.checkAndThrow("Could not notify Data[" + observable + "] to all observer!", exceptionStack);
        return true;
    }
}
