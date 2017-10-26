package org.openbase.jul.pattern.statemachine;

/*-
 * #%L
 * JUL Pattern Default
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

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * Executes states.
 *
 * @author mpohling, malinke
 */
public class StateRunner implements Runnable, Initializable<Class<? extends State>> {

    private static final Logger LOGGER = LoggerFactory.getLogger(StateRunner.class);

    public static final String STATE_CHANGE = "StateChange";
    public static final String STATE_ERROR = "StateError";
    private final Map<Class<? extends State>, State> stateMap;
    private final PropertyChangeSupport change;
    private State currentState;

    public StateRunner() {
        this.stateMap = new HashMap<>();
        this.change = new PropertyChangeSupport(this);
    }

    @Override
    public void init(final Class<? extends State> stateClass) throws InitializationException {
        try {
            currentState = getState(stateClass);
            change.firePropertyChange(STATE_CHANGE, null, currentState.getClass());
        } catch (NotAvailableException ex) {
            throw new InitializationException(this, ex);
        }
    }

    @Override
    public synchronized void run() {
        LOGGER.info("run " + currentState.getClass().getSimpleName() + "...");
        if (currentState == null) {
            throw new IllegalStateException("No initial state defined.");
        }
        while (currentState != null) {
            LOGGER.debug("execute " + currentState);
            final Class<? extends State> nextStateClass;
            try {
                nextStateClass = currentState.call();
            } catch (IOException ex) {
                ExceptionPrinter.printHistory("Somthing went wrong during state execution!", ex, LOGGER);
                try {
                    // todo: why is this needed?
                    Thread.sleep(1000);
                } catch (InterruptedException intEx) {
                    ExceptionPrinter.printHistory("Interruped during sleep!", intEx, LOGGER, LogLevel.WARN);
                    return;
                }
                continue;
            } catch (Throwable t) {
                ExceptionPrinter.printHistory("State failed: ", t, LOGGER);
                change.firePropertyChange(STATE_ERROR, currentState.getClass(), t.getMessage());
                return;
            }
            change.firePropertyChange(STATE_CHANGE, currentState.getClass(), nextStateClass);
            if (nextStateClass != null) {
                LOGGER.info("StateChange: "
                        + currentState.getClass().getSimpleName() + " -> "
                        + nextStateClass.getSimpleName());
                try {
                    currentState = getState(nextStateClass);
                } catch (NotAvailableException ex) {
                    ExceptionPrinter.printHistory("State change failed!", ex, LOGGER);
                }
            }
        }
        LOGGER.info("finished execution.");
    }

    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        change.addPropertyChangeListener(listener);
    }

    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        change.removePropertyChangeListener(listener);
    }

    private State getState(final Class<? extends State> stateClass) throws NotAvailableException {
        if (!stateMap.containsKey(stateClass)) {
            try {
                final State state;
                try {
                    state = stateClass.getConstructor().newInstance();
                } catch (IllegalAccessException | IllegalArgumentException | InstantiationException | NoSuchMethodException | SecurityException | InvocationTargetException ex) {
                    throw new CouldNotPerformException("Could not create instance of " + stateClass.getName(), ex);
                }
                stateMap.put(stateClass, state);
                return state;
            } catch (CouldNotPerformException ex) {
                throw new NotAvailableException("State[" + stateClass.getSimpleName() + "]", ex);
            }
        }
        return stateMap.get(stateClass);
    }

    public State getCurrentState() {
        return currentState;
    }

    public boolean isCurrentState(final Class clazz) {
        return currentState.getClass().equals(clazz);
    }
}
