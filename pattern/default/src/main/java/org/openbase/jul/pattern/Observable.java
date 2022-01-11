package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Shutdownable;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 *
 * @param <S> the type of the data source
 * @param <T> the data type on whose changes is notified
 */
public interface Observable<S, T> extends Shutdownable {

    /**
     * Method blocks until the observable is available. In case the given timeout is reached an
     * TimeoutException is thrown.
     *
     * @param timeout  is the timeout related to the given {@link TimeUnit}.
     * @param timeUnit is the unit of the timeout.
     *
     * @throws InterruptedException     is thrown if the current thread was interrupted externally.
     * @throws CouldNotPerformException is thrown with a TimeoutException cause if the given timeout
     *                                  is reached.
     */
    void waitForValue(final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException;

    /**
     * Method blocks until the observable is available.
     *
     * @throws CouldNotPerformException is thrown if an error occurs before the thread was blocked.
     * @throws InterruptedException     is thrown if the current thread was interrupted externally.
     */
    default void waitForValue() throws CouldNotPerformException, InterruptedException {
        try {
            waitForValue(0, TimeUnit.MILLISECONDS);
        } catch (NotAvailableException ex) {
            // Should never happen because no timeout was given.
            ExceptionPrinter.printHistory(new FatalImplementationErrorException("Observable has notified without valid value!", this, ex), LoggerFactory.getLogger(getClass()));
        }
    }

    /**
     * Method registers the given observer to this observable to get informed about value changes.
     *
     * @param observer is the observer to register.
     */
    void addObserver(Observer<S, T> observer);

    /**
     * Method removes the given observer from this observable to finish the observation.
     *
     * @param observer is the observer to remove.
     */
    void removeObserver(Observer<S, T> observer);

    /**
     * Method returns the latest observable value.
     *
     * @return the latest value of the observable.
     *
     * @throws NotAvailableException
     */
    T getValue() throws NotAvailableException;

    /**
     * Method returns a Future object which represents the value availability.
     *
     * @return a future object providing the value.
     */
    Future<T> getValueFuture();

    /**
     * Checks if a value was ever notified.
     *
     * @return true if the value is available.
     */
    boolean isValueAvailable();

    /**
     * Method returns the latest observable value.
     *
     * @return
     *
     * @throws NotAvailableException
     * @deprecated since v2.0 and will be removed in v3.0. Please  use {@link #getValue()} instead.
     */
    @Deprecated
    default T getLatestValue() throws NotAvailableException {
        return getValue();
    }
}
