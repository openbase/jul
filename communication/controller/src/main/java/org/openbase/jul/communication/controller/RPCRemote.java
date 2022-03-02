package org.openbase.jul.communication.controller;

/*
 * #%L
 * JUL Extension Controller
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

import com.google.protobuf.Message;
import org.openbase.jul.communication.config.CommunicatorConfig;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.TimeoutException;
import org.openbase.jul.pattern.controller.Remote;
import org.openbase.type.communication.ScopeType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * @param <M> the data type of the data which is used for remote synchronization.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface RPCRemote<M extends Message> extends Remote<M> {

    /**
     * Initialize the remote on the given scope.
     *
     * @param scope the scope where the remote communicates
     *
     * @throws InitializationException if the initialization fails
     * @throws InterruptedException    if the initialization is interrupted
     */
    void init(final ScopeType.Scope scope) throws InitializationException, InterruptedException;

    /**
     * Initialize the remote on the given scope.
     *
     * @param scope             the scope where the remote communicates
     * @param communicatorConfig the rsb communicator config which should be used for the connection.
     *
     * @throws InitializationException if the initialization fails
     * @throws InterruptedException    if the initialization is interrupted
     */
    void init(final ScopeType.Scope scope, final CommunicatorConfig communicatorConfig) throws InitializationException, InterruptedException;

    /**
     * Initialize the remote on the given scope.
     *
     * @param scope the scope where the remote communicates
     *
     * @throws InitializationException if the initialization fails
     * @throws InterruptedException    if the initialization is interrupted
     */
    void init(final String scope) throws InitializationException, InterruptedException;

    /**
     * Method returns the scope of this remote connection.
     *
     * @return the remote controller scope.
     *
     * @throws NotAvailableException
     */
    ScopeType.Scope getScope() throws NotAvailableException;

    /**
     * Method synchronously calls the given method without any arguments on the main controller.
     * <p>
     * The method call will block until the call is successfully processed.
     * Even if the main controller instance is currently not reachable, successively retry will be triggered.
     * The only way to cancel the call is an externally interruption of the invoking thread.
     *
     * @param <R>        the return type of the method declaration.
     * @param methodName the method name.
     *
     * @return the return value of the remote method.
     *
     * @throws CouldNotPerformException is thrown in case any error occurred during processing.
     * @throws InterruptedException     is thrown in case the thread was externally interrupted.
     * @deprecated please use async variant.
     */
    @Deprecated
    default <R> R callMethod(final String methodName, final Class<R> returnClazz) throws CouldNotPerformException, InterruptedException {
        try {
            return (R) callMethodAsync(methodName, returnClazz).get();
        } catch (ExecutionException e) {
            throw (CouldNotPerformException) e.getCause();
        }
    }

    /**
     * Method synchronously calls the given method on the main controller.
     * <p>
     * The method call will block until the call is successfully processed.
     * Even if the main controller instance is currently not reachable, successively retry will be triggered.
     * The only way to cancel the call is an externally interruption of the invoking thread.
     *
     * @param <R>        the return type of the method declaration.
     * @param <T>        the argument type of the method.
     * @param methodName the method name.
     * @param argument   the method argument.
     *
     * @return the return value of the remote method.
     *
     * @throws CouldNotPerformException is thrown in case any error occurred during processing.
     * @throws InterruptedException     is thrown in case the thread was externally interrupted.
     * @deprecated please use async variant.
     */
    @Deprecated
    default <R, T extends Object> R callMethod(final String methodName, final Class<R> returnClazz, final T argument) throws CouldNotPerformException, InterruptedException {
        try {
            return (R) callMethodAsync(methodName, returnClazz, argument).get();
        } catch (ExecutionException e) {
            throw (CouldNotPerformException) e.getCause();
        }
    }

    /**
     * Method synchronously calls the given method on the main controller.
     * <p>
     * The method call will block until the call is successfully processed or the given timeout is expired.
     * Even if the main controller instance is currently not reachable, successively retry will be triggered.
     *
     * @param <R>        the return type of the method declaration.
     * @param methodName the method name.
     * @param timeout    the RPC call timeout in milliseconds.
     *
     * @return the return value of the remote method.
     *
     * @throws CouldNotPerformException                    is thrown in case any error occurred during processing.
     * @throws TimeoutException is thrown in case the given timeout is expired before the RPC was successfully processed.
     * @throws InterruptedException                        is thrown in case the thread was externally interrupted.
     * @deprecated please use async variant.
     */
    @Deprecated
    default <R> R callMethod(final String methodName, final Class<R> returnClazz, final long timeout) throws CouldNotPerformException, TimeoutException, InterruptedException {
        try {
            return (R) callMethodAsync(methodName, returnClazz).get(timeout, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new TimeoutException(e);
        } catch (ExecutionException e) {
            throw (CouldNotPerformException) e.getCause();
        }
    }

    /**
     * Method synchronously calls the given method on the main controller.
     * <p>
     * The method call will block until the call is successfully processed or the given timeout is expired.
     * Even if the main controller instance is currently not reachable, successively retry will be triggered.
     *
     * @param <R>        the return type of the method declaration.
     * @param <T>        the argument type of the method.
     * @param methodName the method name.
     * @param argument   the method argument.
     * @param timeout    the RPC call timeout in milliseconds.
     *
     * @return the return value of the remote method.
     *
     * @throws CouldNotPerformException                    is thrown in case any error occurred during processing.
     * @throws TimeoutException is thrown in case the given timeout is expired before the RPC was successfully processed.
     * @throws InterruptedException                        is thrown in case the thread was externally interrupted.
     * @deprecated please use async variant.
     */
    @Deprecated
    default <R, T extends Object> R callMethod(final String methodName, final Class<R> returnClazz, final T argument, final long timeout) throws CouldNotPerformException, TimeoutException, InterruptedException {
        try {
            return (R) callMethodAsync(methodName, returnClazz, argument).get(timeout, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException e) {
            throw new TimeoutException(e);
        } catch (ExecutionException e) {
            throw (CouldNotPerformException) e.getCause();
        }
    }

    /**
     * Method asynchronously calls the given method without any arguments on the main controller.
     *
     * @param <R>        the return type of the method declaration.
     * @param methodName the method name.
     *
     * @return a future instance which gives feedback about the asynchronously method call and when the result is available.
     */
    <R> Future<R> callMethodAsync(final String methodName, final Class<R> returnClazz);

    /**
     * Method asynchronously calls the given method on the main controller.
     *
     * @param <R>        the return type of the method declaration.
     * @param <T>        the argument type of the method.
     * @param methodName the method name.
     * @param argument   the method argument.
     *
     * @return a future instance which gives feedback about the asynchronously method call and when the result is available.
     */
    <R, T extends Object> Future<R> callMethodAsync(final String methodName, final Class<R> returnClazz, final T argument);
}
