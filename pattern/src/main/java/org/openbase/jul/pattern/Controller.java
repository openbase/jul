package org.openbase.jul.pattern;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Changeable;
import org.openbase.jul.iface.Pingable;
import org.openbase.jul.iface.Requestable;
import org.openbase.jul.iface.Shutdownable;

/*
 * #%L
 * JUL Pattern
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M> Message
 */
public interface Controller<M> extends Shutdownable, Activatable, Changeable, Pingable, Requestable<M> {

    // TODO mpohling: Should be moved to rst and reimplement for rsb 14.
    public enum ControllerAvailabilityState {

        ACTIVATING, ONLINE, DEACTIVATING, OFFLINE
    };

    @SuppressWarnings(value = "unchecked")
    public M getData() throws CouldNotPerformException;

    /**
     * Method returns the class of the internal data object which is used for remote synchronization.
     *
     * @return data class
     */
    public Class<M> getDataClass();

    /**
     * Method returns the availability state of this controller.
     *
     * @return OFFLINE / ONLINE
     */
    public ControllerAvailabilityState getControllerAvailabilityState();

    /**
     * Wait until the controller reached a given availability state.
     *
     * @param controllerAvailabilityState the state on which is waited
     * @throws InterruptedException if the waiting is interrupted
     */
    public void waitForAvailabilityState(final ControllerAvailabilityState controllerAvailabilityState) throws InterruptedException;

    /**
     * Synchronize all registered remote instances about a data change.
     *
     * @throws CouldNotPerformException if the notification could not be performed
     * @throws java.lang.InterruptedException if the notification has been interrupted
     */
    @Override
    public void notifyChange() throws CouldNotPerformException, InterruptedException;

}
