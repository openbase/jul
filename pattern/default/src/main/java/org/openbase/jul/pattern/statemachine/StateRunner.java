package org.openbase.jul.pattern.statemachine;

/*-
 * #%L
 * JUL Pattern Default
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Initializable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * A simple state machine implementation which can execute states which are implementing the {@code org.openbase.jul.pattern.statemachine.State} interface.
 *
 * The state graph is defined by the states itself.
 * Whenever a state has finished its task the next state transition is defined by the state itself via the return value of the {@code call} method which defines the next state.
 *
 * Start this state machine via the global executor service after initialization.
 *
 * e.g. GlobalCachedExecutorService.submit(stateMaschineInstance)
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @author malinke
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

    /**
     * Defines the initial state of the state machine.
     *
     * @param stateClass
     * @throws InitializationException
     */
    @Override
    public void init(final Class<? extends State> stateClass) throws InitializationException {
        try {
            currentState = getState(stateClass);
            change.firePropertyChange(STATE_CHANGE, null, currentState.getClass());
        } catch (NotAvailableException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     * Method starts the state machine.
     * Because this class is implementing the runnable interface its not recommended to call this method manually.
     * Start the state machine via the global executor service.
     *
     * e.g. GlobalCachedExecutorService.submit(stateMachineInstance)
     */
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
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Something went wrong during state execution!", ex, LOGGER);
                change.firePropertyChange(STATE_ERROR, currentState.getClass(), ex.getMessage());
                if (Thread.currentThread().isInterrupted()) {
                    return;
                }
                continue;
            } catch (InterruptedException ex) {
                return;
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

    /**
     * Method registers a property change listener which will be informed about state transitions.
     *
     * @param listener the change listener to register.
     */
    public void addPropertyChangeListener(final PropertyChangeListener listener) {
        change.addPropertyChangeListener(listener);
    }

    /**
     * Method removes a previously registered property change listener.
     *
     * @param listener the change listener to remove.
     */
    public void removePropertyChangeListener(final PropertyChangeListener listener) {
        change.removePropertyChangeListener(listener);
    }

    /**
     * Method loads the state referred by the state class.
     *
     * Once the state is loaded it will be cached and next time the state is requested the cached instance will be returned out of performance reasons.
     *
     * @param stateClass the class defining the state to load.
     * @return an new or cached instance of the state..
     * @throws NotAvailableException is thrown if the state could not be loaded.
     */
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
                throw new NotAvailableException(stateClass, ex);
            }
        }
        return stateMap.get(stateClass);
    }

    /**
     * Method returns the state which is currently processed.
     *
     * @return the currently processed state.
     */
    public State getCurrentState() {
        return currentState;
    }

    /**
     * Method can be used to verify if the given state is currently processed.
     *
     * @param clazz the class to define the state type.
     * @return returns true if the given state is currently processed, otherwise false.
     */
    public boolean isCurrentState(final Class clazz) {
        return currentState.getClass().equals(clazz);
    }
}
