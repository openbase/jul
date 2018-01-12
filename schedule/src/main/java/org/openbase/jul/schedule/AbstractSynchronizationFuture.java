/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
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
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * The synchronization future is used to guarantee that the change done by the internal
 * future has at one time been synchronized.
 *
 * @param <T> The return type of the internal future.
 * @author pleminoq
 */
public abstract class AbstractSynchronizationFuture<T> implements Future<T> {

    private final SyncObject CHECK_LOCK = new SyncObject("WaitForUpdateLock");
    private final Observer notifyChangeObserver = (Observer) (Observable source, Object data) -> {
        synchronized (CHECK_LOCK) {
            CHECK_LOCK.notifyAll();
        }
    };

    private final Future<T> internalFuture;
    private final Future synchronisationFuture;

    public AbstractSynchronizationFuture(final Future<T> internalFuture, final DataProvider dataProvider) {
        this.internalFuture = internalFuture;

        // create a synchronisation task which makes sure that the change requested by
        // the internal future has at one time been synchronized to the remote
        synchronisationFuture = GlobalCachedExecutorService.submit(() -> {
            dataProvider.addDataObserver(notifyChangeObserver);
            try {
                T result = internalFuture.get();
                waitForSynchronization(result);
            } finally {
                dataProvider.removeDataObserver(notifyChangeObserver);
            }
            return null;
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return  synchronisationFuture.cancel(mayInterruptIfRunning) && internalFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return synchronisationFuture.isCancelled() && internalFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return synchronisationFuture.isDone() && internalFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        // when get returns without an exception the synchronisation is complete
        // and else the exception will be thrown
        synchronisationFuture.get();

        return internalFuture.get();
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // when get returns without an exception the synchronisation is complete
        // and else the exception will be thrown
        synchronisationFuture.get(timeout, unit);

        // the synchronisation future calls get on the internal future
        // thus if it is done the internal future is also done and does not have
        // to be called with a timeout
        return internalFuture.get();
    }

    public Future<T> getInternalFuture() {
        return internalFuture;
    }

    private void waitForSynchronization(T message) throws CouldNotPerformException {
        beforeWaitForSynchronization();
        synchronized (CHECK_LOCK) {
            try {
                while (!check(message)) {
                    CHECK_LOCK.wait();
                }
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * Called to add an observer to the component whose synchronization is waited for
     * by this future. This is done in this way because sometimes the change is notified
     * through a normal observer and sometime by a data observer so the internal call
     * changes.
     *
     * @param observer In this case always the notify change observer that is added.
     */
    @Deprecated
    protected abstract void addObserver(Observer observer);

    /**
     * Remove the notify change observer from the component whose synchronization is
     * waited for after the synchronization is complete or failed.
     *
     * @param observer In this case always the notify change observer that is added.
     */
    @Deprecated
    protected abstract void removeObserver(Observer observer);

    /**
     * Called before the synchronization task enters its loop. Can for example
     * be used to wait for initial data so that the check that is done afterwards
     * in the loop does not fail immediately.
     *
     * @throws CouldNotPerformException if something goes wrong
     */
    protected abstract void beforeWaitForSynchronization() throws CouldNotPerformException;

    /**
     * Called inside of the synchronization loop to check if the synchronization is complete.
     *
     * @param message the return value of the internal future
     * @return true if the synchronization is complete and else false
     * @throws CouldNotPerformException if something goes wrong in the check
     */
    protected abstract boolean check(final T message) throws CouldNotPerformException;
}
