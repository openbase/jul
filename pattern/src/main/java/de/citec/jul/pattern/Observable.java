/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.pattern;

import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.MultiException.ExceptionStack;
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

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Object LOCK = new Object();
    private final List<Observer<T>> observers;

    public Observable() {
        this.observers = new ArrayList<>();
    }

    public void addObserver(Observer<T> observer) {
        synchronized (LOCK) {
            if (observers.contains(observer)) {
                logger.warn("Skip observer registration. Observer[" + observer + "] is already registered!");
                return;
            }

            observers.add(observer);
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

    public void notifyObservers(T arg) throws MultiException {
        ExceptionStack exceptionStack = null;
        synchronized (LOCK) {
            for (Observer<T> observer : observers) {
                try {
                    observer.update(this, arg);
                } catch (Exception ex) {
                    exceptionStack = MultiException.push(observer, ex, exceptionStack);
                }
            }
        }
        MultiException.checkAndThrow("Could not notify Data[" + arg + "] to all observer!", exceptionStack);
    }
}
