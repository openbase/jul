package org.openbase.jul.pattern;

/*-
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * <p>
 * Class offers the ChangeListener pattern. Where this class offers the handle part which manages a list of listeners and informs those about changes.
 */
public class ChangeHandler implements ChangeListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ChangeHandler.class);

    private final Object LISTENER_LOCK = new Object() {
        @Override
        public String toString() {
            return "ListenerLock";
        }
    };

    private final List<ChangeListener> listeners;

    /**
     * Creates a new handler.
     */
    public ChangeHandler() {
        this.listeners = new ArrayList<>();
    }

    /**
     * Registers the given listener to get informed about changes.
     *
     * @param listener the listener to register.
     */
    public void addlistener(final ChangeListener listener) {
        synchronized (LISTENER_LOCK) {
            if (listeners.contains(listener)) {
                LOGGER.warn("Skip listener registration. listener[" + listener + "] is already registered!");
                return;
            }
            listeners.add(listener);
        }
    }

    /**
     * Removes an already registered listener which is than excluded from further change notifications.
     *
     * @param listener the listener to remove.
     */
    public void removeListener(final ChangeListener listener) {
        synchronized (LISTENER_LOCK) {
            listeners.remove(listener);
        }
    }

    /**
     * Method can be used to inform all registered listeners about a change.
     * <p>
     * Note: In case one listener could not be notified, the others will still be informed about the change and the exception in thrown afterwards.
     *
     * @throws CouldNotPerformException is returned if the notification has failed for some listeners.
     * @throws InterruptedException     is thrown if the thread was externally interrupted.
     */
    @Override
    public void notifyChange() throws CouldNotPerformException, InterruptedException {
        synchronized (LISTENER_LOCK) {
            ExceptionStack exceptionStack = null;
            for (ChangeListener listener : listeners) {
                try {
                    listener.notifyChange();
                } catch (CouldNotPerformException ex) {
                    exceptionStack = MultiException.push(listener, ex, exceptionStack);
                }
            }
            MultiException.checkAndThrow(() ->"Could not notify all listeners about the change!", exceptionStack);
        }
    }
}
