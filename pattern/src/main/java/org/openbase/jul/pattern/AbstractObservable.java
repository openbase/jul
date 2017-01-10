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
import java.util.List;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <T> the data type on whose changes is notified
 */
public abstract class AbstractObservable<T> implements Observable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractObservable.class);

    private static final boolean DEFAULT_UNCHANGED_VALUE_FILTER = true;
    private static final Object DEFAULT_SOURCE = null;

    protected final boolean unchangedValueFilter;
    protected final Object LOCK = new Object();
    protected final List<Observer<T>> observers;
    protected int latestValueHash;
    private Object source;

    /**
     * Construct new Observable.
     */
    public AbstractObservable() {
        this(DEFAULT_UNCHANGED_VALUE_FILTER, DEFAULT_SOURCE);
    }

    /**
     * Construct new Observable.
     *
     * @param source the responsible source of the value notifications.
     */
    public AbstractObservable(final Object source) {
        this(DEFAULT_UNCHANGED_VALUE_FILTER, source);
    }

    /**
     * Construct new Observable
     *
     * @param unchangedValueFilter defines if the observer should be informed even if the value is the same than notified before.
     */
    public AbstractObservable(final boolean unchangedValueFilter) {
        this(unchangedValueFilter, DEFAULT_SOURCE);
    }

    /**
     * Construct new Observable.
     *
     * If the source is not defined the observable itself will be used as notification source.
     *
     * @param unchangedValueFilter defines if the observer should be informed even if the value is the same than notified before.
     * @param source the responsible source of the value notifications.
     */
    public AbstractObservable(final boolean unchangedValueFilter, final Object source) {
        this.observers = new ArrayList<>();
        this.unchangedValueFilter = unchangedValueFilter;
        this.source = source == DEFAULT_SOURCE ? this : source; // use observer itself if source was not explicit defined.
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
            try {
                if (observable == null) {
                    LOGGER.debug("Skip notification because observable is null!");
                    return false;
                }
                if (unchangedValueFilter && isValueAvailable() && observable.hashCode() == latestValueHash) {
                    LOGGER.debug("#+# Skip notification because observable has not been changed!");
                    return false;
                }

                latestValueHash = observable.hashCode();

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
