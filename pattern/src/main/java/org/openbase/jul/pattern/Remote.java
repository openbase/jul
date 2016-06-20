package org.openbase.jul.pattern;

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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Shutdownable;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M>
 */
public interface Remote<M> extends Shutdownable, Activatable {

    // TODO mpohling: Should be moved to rst and reimplement for rsb 15.
    public enum RemoteConnectionState {

        UNKNOWN, CONNECTING, CONNECTED, DISCONNECTED
    };

    /**
     * Method synchronously calls the given method without any arguments on the main controller.
     *
     * The method call will block until the call is successfully processed.
     * Even if the main controller instance is currently not reachable, successively retry will be triggered.
     * The only way to cancel the call is an externally interruption of the invoking thread.
     *
     * @param <R> the return type of the method declaration.
     * @param methodName the method name.
     * @return a future instance which gives feedback about the asynchronously method call and when the result is available.
     * @throws CouldNotPerformException is thrown in case any error occurred during processing.
     * @throws InterruptedException is thrown in case the thread was externally interrupted.
     */
    public <R> R callMethod(final String methodName) throws CouldNotPerformException, InterruptedException;

    /**
     * Method synchronously calls the given method on the main controller.
     *
     * The method call will block until the call is successfully processed.
     * Even if the main controller instance is currently not reachable, successively retry will be triggered.
     * The only way to cancel the call is an externally interruption of the invoking thread.
     *
     * @param <R> the return type of the method declaration.
     * @param <T> the argument type of the method.
     * @param methodName the method name.
     * @param argument the method argument.
     * @return a future instance which gives feedback about the asynchronously method call and when the result is available.
     * @throws CouldNotPerformException is thrown in case any error occurred during processing.
     * @throws InterruptedException is thrown in case the thread was externally interrupted.
     */
    public <R, T extends Object> R callMethod(final String methodName, final T argument) throws CouldNotPerformException, InterruptedException;

    /**
     * Method asynchronously calls the given method without any arguments on the main controller.
     *
     * @param <R> the return type of the method declaration.
     * @param methodName the method name.
     * @return a future instance which gives feedback about the asynchronously method call and when the result is available.
     * @throws CouldNotPerformException is thrown in case any error occurred during processing.
     */
    public <R> Future<R> callMethodAsync(final String methodName) throws CouldNotPerformException;

    /**
     * Method asynchronously calls the given method on the main controller.
     *
     * @param <R> the return type of the method declaration.
     * @param <T> the argument type of the method.
     * @param methodName the method name.
     * @param argument the method argument.
     * @return a future instance which gives feedback about the asynchronously method call and when the result is available.
     * @throws CouldNotPerformException is thrown in case any error occurred during processing.
     */
    public <R, T extends Object> Future<R> callMethodAsync(final String methodName, final T argument) throws CouldNotPerformException;

    /**
     *
     * @param scope
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final String scope) throws InitializationException, InterruptedException;

    /**
     * This method allows the registration of data observers to get informed about data updates.
     *
     * @param observer
     */
    public void addDataObserver(final Observer<M> observer);

    /**
     * This method removes already registered data observers.
     *
     * @param observer
     */
    public void removeDataObserver(final Observer<M> observer);

    /**
     * Check if the data object is already available.
     *
     * @return
     */
    public boolean isDataAvailable();

    /**
     * Method returns the class of the data object.
     *
     * @return
     */
    public Class<M> getDataClass();

    /**
     * Method returns the data object of this remote which is synchronized with
     * the server data in background.
     *
     * In case the data was never received not available a NotAvailableException
     * is thrown. Use method getDataFuture()
     *
     * @return
     * @throws CouldNotPerformException
     */
    public M getData() throws CouldNotPerformException;

    /**
     * Returns a future of the data object. This method can be useful after
     * remote initialization in case the data object was not received jet. The
     * future can be used to wait for the data object.
     *
     * @return a future object delivering the data if available.
     * @throws CouldNotPerformException In case something went wrong a
     * CouldNotPerformException is thrown.
     */
    public CompletableFuture<M> getDataFuture() throws CouldNotPerformException;

    /**
     * Method blocks until an initial data message was received from the remote controller.
     *
     * @throws CouldNotPerformException is thrown if any error occurs.
     * @throws InterruptedException is thrown in case the thread is externally interrupted.
     */
    public void waitForData() throws CouldNotPerformException, InterruptedException;

    /**
     * Method blocks until an initial data message was received from the main controller or the given timeout is reached.
     *
     * @param timeout maximal time to wait for the main controller data. After the timeout is reached a TimeoutException is thrown.
     * @param timeUnit the time unit of the timeout.
     * @throws CouldNotPerformException is thrown in case the any error occurs, or if the given timeout is reached. In this case a TimeoutException is thrown.
     */
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException;

    /**
     * Checks if a server connection is established.
     *
     * @return is true in case the connection is established.
     */
    public boolean isConnected();

    /**
     * Method returns the current connection state between this remote and its main controller.
     *
     * @return the current connection state.
     */
    public RemoteConnectionState getConnectionState();

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
     * @return A CompletableFuture which gives feedback about the successful
     * synchronization.
     * @throws CouldNotPerformException In case the sync could not be triggered
     * an CouldNotPerformException will be thrown.
     */
    public CompletableFuture<M> requestData() throws CouldNotPerformException;
}
