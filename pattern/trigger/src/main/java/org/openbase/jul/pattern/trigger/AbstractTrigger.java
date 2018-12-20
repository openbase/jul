package org.openbase.jul.pattern.trigger;

/*-
 * #%L
 * JUL Pattern Trigger
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.rst.processing.TimestampProcessor;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.state.ActivationStateType.ActivationState;

/**
 *
 * @author <a href="mailto:tmichalski@techfak.uni-bielefeld.de">Timo Michalski</a>
 */
public abstract class AbstractTrigger implements Shutdownable, Trigger {

    private final ObservableImpl<Trigger, ActivationState> triggerObservable;

    public AbstractTrigger() throws InstantiationException {
        this.triggerObservable = new ObservableImpl<>(this);

        try {
            this.triggerObservable.notifyObservers(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.UNKNOWN).build()));
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("Could not set initial state", ex);
        }
    }

    @Override
    public ActivationState getActivationState() throws NotAvailableException {
        return triggerObservable.getValue();
    }

    @Override
    public void addObserver(Observer<Trigger, ActivationState> observer) {
        triggerObservable.addObserver(observer);
    }

    @Override
    public void removeObserver(Observer<Trigger, ActivationState> observer) {
        triggerObservable.removeObserver(observer);
    }

    protected void notifyChange(final ActivationState newState) throws CouldNotPerformException {
        if (!triggerObservable.getValue().getValue().equals(newState.getValue())) {
            triggerObservable.notifyObservers(newState);
        }
    }

    @Override
    public void shutdown() {
        triggerObservable.shutdown();
        try {
            deactivate();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not shutdown " + this, ex, LoggerFactory.getLogger(getClass()));
        }
    }
}
