package org.dc.jul.pattern;

/*
 * #%L
 * JUL Pattern
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Activatable;
import org.dc.jul.iface.Shutdownable;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine
 * Threepwood</a>
 * @param <M>
 */
public interface Remote<M> extends Shutdownable, Activatable, Observable<M> {

    public Object callMethod(String methodName) throws CouldNotPerformException, InterruptedException;

    public <R, T extends Object> R callMethod(String methodName, T type) throws CouldNotPerformException, InterruptedException;

    public Future<Object> callMethodAsync(String methodName) throws CouldNotPerformException;

    public <R, T extends Object> Future<R> callMethodAsync(String methodName, T type) throws CouldNotPerformException;

    public void init(final String scope) throws InitializationException, InterruptedException;

    public void notifyUpdated(M data) throws CouldNotPerformException;

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
     * Checks if a server connection is established.
     * @return 
     */
    public boolean isConnected();

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
