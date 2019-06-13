package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern Default
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

/**
 * @param <S> the type of the data source
 * @param <T> the data type on whose changes is notified
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractObservable<S, T> implements Observable<S, T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractObservable.class);

    private static final boolean DEFAULT_UNCHANGED_VALUE_FILTER = true;

    protected final boolean unchangedValueFilter;
    protected final List<Observer<S, T>> observers;
    private final Object OBSERVER_LOCK = new Object() {
        @Override
        public String toString() {
            return "ObserverLock";
        }
    };

    private final Object NOTIFICATION_MESSAGE_LOCK = new Object() {
        @Override
        public String toString() {
            return "NotificationMessageLock";
        }
    };
    private final Object NOTIFICATION_PROGRESS_LOCK = new Object() {
        @Override
        public String toString() {
            return "NotificationProgressLock";
        }
    };
    protected int latestValueHash;
    private boolean notificationInProgress = false;
    private S source;
    private ExecutorService executorService;
    private HashGenerator<T> hashGenerator;

    private boolean shutdownInitiated = false;

    /**
     * Construct new Observable.
     */
    public AbstractObservable() {
        this(DEFAULT_UNCHANGED_VALUE_FILTER, null);
    }

    /**
     * Construct new Observable.
     *
     * @param source the responsible source of the value notifications.
     */
    public AbstractObservable(final S source) {
        this(DEFAULT_UNCHANGED_VALUE_FILTER, source);
    }

    /**
     * Construct new Observable
     *
     * @param unchangedValueFilter defines if the observer should be informed even if the value is
     *                             the same than notified before.
     */
    public AbstractObservable(final boolean unchangedValueFilter) {
        this(unchangedValueFilter, null);
    }

    /**
     * Construct new Observable.
     * <p>
     * If the source is not defined the observable itself will be used as notification source.
     *
     * @param unchangedValueFilter defines if the observer should be informed even if the value is
     *                             the same than notified before.
     * @param source               the responsible source of the value notifications.
     */
    public AbstractObservable(final boolean unchangedValueFilter, final S source) {
        this.observers = new ArrayList<>();
        this.unchangedValueFilter = unchangedValueFilter;
        this.source = source;
        this.hashGenerator = new HashGenerator<T>() {
            @Override
            public int computeHash(T value) throws CouldNotPerformException {
                try {
                    return value.hashCode();
                } catch (ConcurrentModificationException ex) {
                    throw new FatalImplementationErrorException("Observable has changed during hash computation in notification! Set a HashGenerator for the observable to control the hash computation yourself!", this, ex);
                }
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void addObserver(final Observer<S, T> observer) {
        synchronized (OBSERVER_LOCK) {
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
     * @param observer {@inheritDoc}
     */
    @Override
    public void removeObserver(final Observer<S, T> observer) {
        synchronized (OBSERVER_LOCK) {
            observers.remove(observer);
        }
    }

    /**
     * {@inheritDoc}
     * <p>
     * Note: Ongoing notifications are skipped during shutdown.
     */
    @Override
    public void shutdown() {
        synchronized (OBSERVER_LOCK) {
            observers.clear();
        }
    }

    /**
     * Notify all changes of the observable to all observers only if the observable has changed. The
     * source of the notification is set as this. Because of data encapsulation reasons this method
     * is not included within the Observer interface.
     * Attention! This method is not thread safe against changes of the observable because the check if the observable has changed is
     * done by computing its hash value. Therefore if the observable is a collection and it is changed
     * while notifying a concurrent modification exception can occur. To avoid this compute the
     * observable hash yourself by setting a hash generator.
     * If this method is interrupted a rollback is done by reseting the latestHashValue. Thus the observable
     * has not changed and false is returned.
     *
     * @param observable the value which is notified
     *
     * @return true if the observable has changed
     *
     * @throws MultiException           thrown if the notification to at least one observer fails
     * @throws CouldNotPerformException thrown if the hash computation fails
     */
    public boolean notifyObservers(final T observable) throws MultiException, CouldNotPerformException {
        return notifyObservers(source, observable);
    }

    /**
     * Notify all changes of the observable to all observers only if the observable has changed.
     * Because of data encapsulation reasons this method is not included within the Observer
     * interface.
     * Attention! This method is not thread safe against changes of the observable because the check if the observable has changed is
     * done by computing its hash value. Therefore if the observable is a collection and it is changed
     * while notifying a concurrent modification exception can occur. To avoid this compute the
     * observable hash yourself by setting a hash generator.
     * If this method is interrupted a rollback is done by reseting the latestHashValue. Thus the observable
     * has not changed and false is returned.
     * <p>
     * Note: In case the given observable is null this notification will be ignored.
     *
     * @param source     the source of the notification
     * @param observable the value which is notified
     *
     * @return true if the observable has changed
     *
     * @throws MultiException           thrown if the notification to at least one observer fails
     * @throws CouldNotPerformException thrown if the hash computation fails
     */
    public boolean notifyObservers(final S source, final T observable) throws MultiException, CouldNotPerformException {
        synchronized (NOTIFICATION_MESSAGE_LOCK) {
            long wholeTime = System.currentTimeMillis();
            if (observable == null) {
                LOGGER.debug("Skip notification because observable is null!");
                return false;
            }

            ExceptionStack exceptionStack = null;
            final Map<Observer<S, T>, Future<Void>> notificationFutureList = new HashMap<>();

            final ArrayList<Observer<S, T>> tempObserverList;

            try {
                synchronized (NOTIFICATION_PROGRESS_LOCK) {
                    notificationInProgress = true;
                }
                final int observableHash = hashGenerator.computeHash(observable);
                if (unchangedValueFilter && isValueAvailable() && observableHash == latestValueHash) {
                    LOGGER.debug("Skip notification because " + this + " has not been changed!");
                    return false;
                }

                applyValueUpdate(observable);
                final int lastHashValue = latestValueHash;
                latestValueHash = observableHash;

                synchronized (OBSERVER_LOCK) {
                    tempObserverList = new ArrayList<>(observers);
                }

                for (final Observer<S, T> observer : tempObserverList) {

                    // skip ongoing notifications if shutdown was initiated or the thread was interrupted.
                    if (shutdownInitiated && Thread.currentThread().isInterrupted()) {
                        latestValueHash = lastHashValue;
                        return false;
                    }

                    if (executorService == null) {

                        // synchronous notification
                        long time = System.currentTimeMillis();
                        try {
                            observer.update(source, observable);
                            time = System.currentTimeMillis() - time;
                            if (time > 500) {
                                LOGGER.debug("Notification to observer[{}] took: {}ms", observer, time);
                            }
                        } catch (InterruptedException ex) {
                            latestValueHash = lastHashValue;
                            Thread.currentThread().interrupt();
                            return false;
                        } catch (Exception ex) {
                            //TODO I do not know if this is useful generally, but it helps debugging if invalid service state observers are registered on a unit
                            if (ex instanceof ClassCastException) {
                                LOGGER.error("Probably defect Observer[{}] registered on {}", observer, this);
                            }
                            exceptionStack = MultiException.push(observer, new CouldNotPerformException("Observer["+observer.getClass().getSimpleName()+"] update failed!", ex), exceptionStack);
                        }
                    } else {
                        // asynchronous notification
                        notificationFutureList.put(observer, executorService.submit(() -> {
                            observer.update(source, observable);
                            return null;
                        }));
                    }
                }
            } finally {
                assert observable != null;
                synchronized (NOTIFICATION_PROGRESS_LOCK) {
                    notificationInProgress = false;
                    NOTIFICATION_PROGRESS_LOCK.notifyAll();
                }
            }

            //TODO: this check is wrong -> != but when implemented correctly leads bco not starting
            // handle exeception printing for async variant
            if (executorService == null) {
                for (final Entry<Observer<S, T>, Future<Void>> notificationFuture : notificationFutureList.entrySet()) {
                    try {
                        notificationFuture.getValue().get();
                    } catch (InterruptedException ex) {
                        Thread.currentThread().interrupt();
                        return true;
                    } catch (Exception ex) {
                        exceptionStack = MultiException.push(notificationFuture.getKey(), new CouldNotPerformException("Observer["+notificationFuture.getKey().getClass().getSimpleName()+"] update failed!", ex) , exceptionStack);
                    }
                }
            }

            MultiException.checkAndThrow(() -> {
                String stringRep = observable.toString();
                if (stringRep.length() > 80) {
                    stringRep = stringRep.substring(0, 80) + " [...]";
                }
                return "Could not notify Data[" + stringRep + "] to all observer!";
            }, exceptionStack);

            wholeTime = System.currentTimeMillis() - wholeTime;
            if (wholeTime > 500) {
                LOGGER.debug("Notification on observable[{}] took: {}ms", observable.getClass().getName(), wholeTime);
            }
            return true;
        }
    }

    /**
     * Method is called if a observer notification delivers a new value.
     * Note: Overwrite this method for getting informed about value changes.
     *
     * @param value the new value
     */
    protected void applyValueUpdate(final T value) throws NotAvailableException {
        // overwrite for current state holding obervable implementations.
    }

    /**
     * Set an executor service for the observable. If it is set the notification
     * will be parallelized using this service.
     *
     * @param executorService the executor service which will be used for parallelization
     */
    public void setExecutorService(ExecutorService executorService) {
        this.executorService = executorService;
    }

    public void setHashGenerator(HashGenerator<T> hashGenerator) {
        this.hashGenerator = hashGenerator;
    }

    /**
     * Method checks if a notification is currently in progess.
     *
     * @return notificationInProgress returns true if a notification is currently in progress.
     */
    public boolean isNotificationInProgress() {
        return notificationInProgress;
    }

    public void waitUntilNotificationIsFinished() throws InterruptedException {
        synchronized (NOTIFICATION_PROGRESS_LOCK) {
            // wait for ongoing notification.
            if (notificationInProgress) {
                NOTIFICATION_PROGRESS_LOCK.wait();
            }
        }
    }

    @Override
    public String toString() {
        return Observable.class.getSimpleName() + "[" + (source == null || source == this ? "" : source.getClass().getSimpleName() + "]");
    }
}
