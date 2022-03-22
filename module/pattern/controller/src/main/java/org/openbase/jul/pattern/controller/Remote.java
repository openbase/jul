package org.openbase.jul.pattern.controller;

/*
 * #%L
 * JUL Pattern Controller
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.openbase.jul.pattern.CompletableFutureLite;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.TimeoutException;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Lockable;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.iface.provider.PingProvider;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.type.domotic.state.ConnectionStateType.ConnectionState;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M> the data type of the remote
 */
public interface Remote<M> extends Shutdownable, Activatable, Lockable, PingProvider, DataProvider<M> {

    /**
     * Method activates the remote instance and blocks until the first data synchronization is done.
     *
     * Equivalent of: activate(); waitForData(0);
     *
     * Caution: Method can blocks forever if the related main controller instance will be never available!
     *
     * @param waitForData if this flag is true the method will block until the first data synchronization is done.
     * @throws CouldNotPerformException if the activation could not be performed
     * @throws InterruptedException if the activation is interrupted
     */
    void activate(boolean waitForData) throws CouldNotPerformException, InterruptedException;

    /**
     * Atomic activate which makes sure that the maintainer stays the same.
     * This method just locks the remote with the given maintainer after activation if it was never locked before.
     *
     * @param maintainer the current maintainer of this remote
     * @throws InterruptedException        if activation is interrupted
     * @throws CouldNotPerformException    if activation fails
     */
    void activate(final Object maintainer) throws InterruptedException, CouldNotPerformException;

    /**
     * This method allows the registration of connection state observers to get informed about connection state changes.
     *
     * @param observer the observer added
     */
    void addConnectionStateObserver(final Observer<Remote<?>, ConnectionState.State> observer);

    /**
     * This method removes already registered connection state observers.
     *
     * @param observer the observer removed
     */
    void removeConnectionStateObserver(final Observer<Remote<?>, ConnectionState.State> observer);

    /**
     * Method returns the class of the data object.
     *
     * @return the class of the data object
     */
    @Override
    Class<M> getDataClass();

    /**
     * Method returns the data object of this remote which is synchronized with
     * the server data in background.
     *
     * In case the data was never received not available a NotAvailableException is thrown.
     * Use method getDataFuture() to get feedback about the synchronization state, or use method waitForData() to block until the needed data is synchronized.
     *
     * @return the data object of the remote.
     * @throws NotAvailableException is thrown in case the data is not yet synchronized with the main controller instance.
     */
    @Override
    M getData() throws NotAvailableException;

    /**
     * Returns a future of the data object. This method can be useful after
     * remote initialization in case the data object was not received jet. The
     * future can be used to wait for the data object.
     *
     * @return a future object delivering the data if available.
     */
    @Override
    default Future<M> getDataFuture() {
        try {
            if (!isDataAvailable()) {
                return requestData();
            }
            return FutureProcessor.completedFuture(getData());

        } catch (CouldNotPerformException ex) {
            CompletableFutureLite<M> future = new CompletableFutureLite<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * Method blocks until an initial data message was received from the remote controller.
     *
     * @throws CouldNotPerformException is thrown if any error occurs.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    @Override
    void waitForData() throws CouldNotPerformException, InterruptedException;

    /**
     * Method blocks until an initial data message was received from the main controller or the given timeout is reached.
     *
     * @param timeout maximal time to wait for the main controller data. After the timeout is reached a NotAvailableException is thrown which is caused by a TimeoutException.
     * @param timeUnit the time unit of the timeout.
     * @throws CouldNotPerformException is thrown in case the any error occurs, or if the given timeout is reached. In this case a TimeoutException is thrown.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    @Override
    void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException;

    /**
     * Checks if a server connection is established.
     *
     * @return is true in case the connection is established.
     */
    boolean isConnected();

    /**
     * Method returns the current connection state between this remote and its main controller.
     *
     * @return the current connection state.
     */
    ConnectionState.State getConnectionState();

    /**
     * Method blocks until the remote reaches the desired connection state. In
     * case the timeout is expired an TimeoutException will be thrown.
     *
     * @param connectionState the desired connection state
     * @param timeout         the timeout in milliseconds until the method throw a
     *                        TimeoutException in case the connection state was not reached.
     * @throws InterruptedException                                is thrown in case the thread is externally
     *                                                             interrupted.
     * @throws org.openbase.jul.exception.TimeoutException         is thrown in case the
     *                                                             timeout is expired without reaching the connection state.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the connection state does not match and the shutdown of this remote
     *                                                             has been initialized
     */
    void waitForConnectionState(final ConnectionState.State connectionState, long timeout) throws InterruptedException, TimeoutException, CouldNotPerformException;

    /**
     * This method synchronizes this remote instance with the main controller
     * and returns the new data object. Normally, all server data changes are
     * automatically synchronized to all remote instances. In case you have
     * triggered many server changes, this changes are sequentially applied.
     * With this method you can force the sync to get instantly a data object
     * with all applied changes. This action can not be canceled! Use this
     * method with caution because high frequently calls will reduce the network
     * performance! The preferred by to access the data object
     *
     * @return A Future which gives feedback about the successful synchronization.
     */
    Future<M> requestData();

    /**
     * Method triggers a ping between this remote and its main controller and
     * returns the calculated connection delay. This method is triggered
     * automatically in background to check if the main controller is still
     * available.
     *
     * @return the connection delay in milliseconds.
     */
    @Override
    Future<Long> ping();

    /**
     * Method returns the result of the latest connection ping between this
     * remote and its main controller.
     *
     * @return the latest connection delay in milliseconds.
     */
    @Override
    Long getPing();
}
