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
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M> Message
 * @param <MB> Message Builder
 */
public interface Controller<M, MB> extends Shutdownable, Activatable, Changeable, Pingable, Requestable<M> {

    // TODO mpohling: Should be moved to rst and reimplement for rsb 14.
    public enum ControllerAvailabilityState {
        LAUNCH, ONLINE, SHUTDOWN, OFFLINE
    };

    public MB cloneDataBuilder();

    @SuppressWarnings(value = "unchecked")
    public M getData() throws CouldNotPerformException;

    /**
     * This method generates a closable data builder wrapper including the
     * internal builder instance. Be informed that the internal builder is
     * directly locked and all internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the builder lock after
     * you builder modifications, otherwise the overall processing pipeline is
     * delayed.
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try (ClosableDataBuilder<MotionSensor.Builder> dataBuilder = getDataBuilder(this)) {
     *         dataBuilder.getInternalBuilder().setMotionState(motion);
     *     } catch (Exception ex) {
     *         throw new CouldNotPerformException("Could not apply data change!", ex);
     *     }
     * }
     * </pre> In this example the ClosableDataBuilder.close method is be called
     * in background after leaving the try brackets.
     *
//     * @param consumer
//     * @return a new builder wrapper with a locked builder instance.
     * @return 
     */
    //TODO: Should be implemented as interface.
//    public ClosableDataBuilder<MB> getDataBuilder(final Object consumer);

    /**
     * Method returns the class of the internal data object which is used for remote synchronization.
     * @return data class
     */
    public Class<M> getDataClass();

    /**
     * Method returns the availability state of this controller.
     * @return OFFLINE / ONLINE
     */
    public ControllerAvailabilityState getControllerAvailabilityState();

    /**
     * Synchronize all registered remote instances about a data change.
     *
     * @throws CouldNotPerformException
     * @throws java.lang.InterruptedException
     */
    @Override
    public void notifyChange() throws CouldNotPerformException, InterruptedException;

}
