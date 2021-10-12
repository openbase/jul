package org.openbase.jul.pattern.trigger;

/*-
 * #%L
 * JUL Pattern Trigger
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

import java.util.ArrayList;
import java.util.List;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.type.processing.TimestampProcessor;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.openbase.type.domotic.state.ActivationStateType.ActivationState;

/**
 * @author <a href="mailto:tmichalski@techfak.uni-bielefeld.de">Timo Michalski</a>
 */
public class TriggerPool extends AbstractTrigger {

    public enum TriggerAggregation {
        AND, OR
    }

    private final static Logger LOGGER = LoggerFactory.getLogger(AbstractTrigger.class);

    private boolean active;

    private final List<Trigger> triggerListAND;
    private final List<Trigger> triggerListOR;
    private final Observer<Trigger, ActivationState> triggerAndObserver;
    private final Observer<Trigger, ActivationState> triggerOrObserver;

    public TriggerPool() throws InstantiationException {
        triggerListAND = new ArrayList<>();
        triggerListOR = new ArrayList<>();
        active = false;

        triggerAndObserver = (Trigger source, ActivationState data) -> {
            verifyCondition();
        };

        triggerOrObserver = (Trigger source, ActivationState data) -> {
            verifyCondition();
        };

        try {
            notifyChange(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.UNKNOWN).build()));
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException("Could not set initial state", ex);
        }
    }

    public void addTrigger(Trigger trigger, TriggerAggregation triggerAggregation) throws CouldNotPerformException {
        if (triggerAggregation == TriggerAggregation.AND) {
            triggerListAND.add(trigger);
        } else {
            triggerListOR.add(trigger);
        }
        if (active) {
            switch (triggerAggregation) {
                case OR:
                    trigger.addObserver(triggerOrObserver);
                    break;
                case AND:
                    trigger.addObserver(triggerAndObserver);
                    break;
            }

            try {
                trigger.activate();
            } catch (InterruptedException ex) {
                throw new CouldNotPerformException("Could not activate Trigger.", ex);
            }

            try {
                verifyCondition();
            } catch (NotAvailableException ex) {
                //ExceptionPrinter.printHistory("Data not available " + trigger, ex, LoggerFactory.getLogger(getClass()));
            }
        }
    }

    public void removeTrigger(AbstractTrigger trigger) {
        if (triggerListAND.contains(trigger)) {
            trigger.removeObserver(triggerAndObserver);
            triggerListAND.remove(trigger);
        } else if (triggerListOR.contains(trigger)) {
            trigger.removeObserver(triggerOrObserver);
            triggerListOR.remove(trigger);
        }
    }

    private void verifyCondition() throws CouldNotPerformException {
        if (verifyOrCondition() || verifyAndCondition()) {
            if (!getActivationState().getValue().equals(ActivationState.State.ACTIVE)) {
                notifyChange(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.ACTIVE).build()));
            }
        } else {
            if (!getActivationState().getValue().equals(ActivationState.State.INACTIVE)) {
                notifyChange(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.INACTIVE).build()));
            }
        }
    }

    private boolean verifyAndCondition() throws CouldNotPerformException {

        if(triggerListAND.isEmpty()) {
            return false;
        }

        return triggerListAND
                .stream()
                .allMatch(trigger -> {
                    try {
                        return trigger.getActivationState().getValue() == ActivationState.State.ACTIVE;
                    } catch (NotAvailableException exception) {
                        return false;
                    }
                });
    }

    private boolean verifyOrCondition() throws CouldNotPerformException {
        return triggerListOR
                .stream()
                .anyMatch(trigger -> {
                    try {
                        return trigger.getActivationState().getValue() == ActivationState.State.ACTIVE;
                    } catch (NotAvailableException exception) {
                        return false;
                    }
                });
    }

    /**
     * return the total amount of registered triggers.
     *
     * @return the total amount of trigger.
     */
    public int getSize() {
        return triggerListAND.size() + triggerListOR.size();
    }

    /**
     * Check if pool contains any trigger.
     *
     * @return true if no trigger are registered.
     */
    public boolean isEmpty() {
        return triggerListAND.isEmpty() && triggerListOR.isEmpty();
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        for (Trigger trigger : triggerListAND) {
            trigger.addObserver(triggerAndObserver);
            trigger.activate();
        }
        for (Trigger trigger : triggerListOR) {
            trigger.addObserver(triggerOrObserver);
            trigger.activate();
        }
        verifyCondition();
        active = true;
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        for (Trigger trigger : triggerListAND) {
            trigger.removeObserver(triggerAndObserver);
            trigger.deactivate();
        }
        for (Trigger trigger : triggerListOR) {
            trigger.removeObserver(triggerOrObserver);
            trigger.deactivate();
        }
        notifyChange(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.UNKNOWN).build()));
        active = false;
    }

    @Override
    public boolean isActive() {
        return active;
    }

    public void forceNotification() throws CouldNotPerformException {
        try {
            if (verifyOrCondition() || verifyAndCondition()) {
                notifyChange(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.ACTIVE).build()));
            } else {
                notifyChange(TimestampProcessor.updateTimestampWithCurrentTime(ActivationState.newBuilder().setValue(ActivationState.State.INACTIVE).build()));
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not force notify trigger pool!", ex);
        }
    }

    @Override
    public void shutdown() {
        try {
            deactivate();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not shutdown " + this, ex, LoggerFactory.getLogger(getClass()));
        }
        super.shutdown();
    }
}
