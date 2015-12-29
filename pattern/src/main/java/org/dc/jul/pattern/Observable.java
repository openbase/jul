/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.pattern;

import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.MultiException.ExceptionStack;
import org.dc.jul.exception.NotAvailableException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 * @param <T>
 */
public class Observable<T> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Observable.class);

    private static final boolean DEFAULT_UNCHANGED_DATA_FILTER = true;

    private final boolean unchangedDataFilter;
    private final Object LOCK = new Object();
    private final List<Observer<T>> observers;
    private T latestValue;

    public Observable() {
        this(DEFAULT_UNCHANGED_DATA_FILTER);
    }

    public Observable(final boolean unchangedDataFilter) {
        this.observers = new ArrayList<>();
        this.unchangedDataFilter = unchangedDataFilter;
    }

    public void addObserver(Observer<T> observer) {
        synchronized (LOCK) {
            if (observers.contains(observer)) {
                LOGGER.warn("Skip observer registration. Observer[" + observer + "] is already registered!");
                return;
            }

            observers.add(observer);
            //TODO mpohling: check if this is usefull and does not interfere with any usage.
//            try {
//                observer.update(this, latestValue);
//            } catch (Exception ex) {
//                ExceptionPrinter.printHistory(new CouldNotPerformException("Initial Observer[" + observer + "] sync failed!", ex), logger, LogLevel.ERROR);
//            }
        }
    }

    public void removeObserver(Observer<T> observer) {
        synchronized (LOCK) {
            observers.remove(observer);
        }
    }

    public void shutdown() {
        synchronized (LOCK) {
            observers.clear();
        }
    }

    public void notifyObservers(Observable<T> source, T observable) throws MultiException {
        ExceptionStack exceptionStack = null;

        synchronized (LOCK) {
            //TODO mpohling: check why this is not working!
//            if (observable == null) {
//                logger.debug("Skip notification because observable is null!");
//                return;
//            }
//
//            if (unchangedDataFilter && latestValue != null && latestValue.equals(observable) && latestValue.toString().equals(observable.toString())) {
//                try {
//                    Thread.sleep(100);
//                } catch (InterruptedException ex) {
//                    java.util.logging.Logger.getLogger(Observable.class.getName()).log(Level.SEVERE, null, ex);
//                }
//                logger.debug("Skip notification because observable has not changed!");
//                return;
//            }

            latestValue = observable;

            for (Observer<T> observer : observers) {
                try {
                    observer.update(source, observable);
                } catch (Exception ex) {
                    exceptionStack = MultiException.push(observer, ex, exceptionStack);
                }
            }
        }
        MultiException.checkAndThrow("Could not notify Data[" + observable + "] to all observer!", exceptionStack);
    }

    public void notifyObservers(T observable) throws MultiException {
        notifyObservers(this, observable);
    }

    public T getLatestValue() throws NotAvailableException {
        if (latestValue == null) {
            throw new NotAvailableException("latestvalue");
        }
        return latestValue;
    }
}
