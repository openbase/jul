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
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.SyncObject;

/**
 * The synchronization future is used to guarantee that the change done by the internal
 * future has at one time been synchronized.
 *
 * @author pleminoq
 * @param <T> The return type of the internal future.
 */
public abstract class AbstractSynchronizationFuture<T> implements Future<T> {

    private final SyncObject CHECK_LOCK = new SyncObject("WaitForUpdateLock");
    private final SyncObject SYNCHRONISTION_LOCK = new SyncObject("SynchronisationLock");
    private final Observer notifyChangeObserver = (Observer) (Observable source, Object data) -> {
        synchronized (CHECK_LOCK) {
            CHECK_LOCK.notifyAll();
        }
    };
    private boolean synchronisationComplete = false;

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
                synchronized (SYNCHRONISTION_LOCK) {
                    synchronisationComplete = true;
                    SYNCHRONISTION_LOCK.notifyAll();
                }
            } catch (InterruptedException ex) {
                // restore interrput
                Thread.currentThread().interrupt();
            } catch (ExecutionException ex) {
                // can only happen if the internal future failed so do nothing
                // because errors on the internal future are received by calling
                // get on this future anyways
            } catch (CouldNotPerformException ex) {
                // can only happen if waitForSynchronization failed
                // so throw the excepion so that it is clear that this task failed
                throw ex;
            } finally {
                dataProvider.removeDataObserver(notifyChangeObserver);
                synchronized (SYNCHRONISTION_LOCK) {
                    SYNCHRONISTION_LOCK.notifyAll();
                }
            }
            return null;
        });
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        return internalFuture.cancel(mayInterruptIfRunning) && synchronisationFuture.cancel(mayInterruptIfRunning);
    }

    @Override
    public boolean isCancelled() {
        return internalFuture.isCancelled() && synchronisationFuture.isCancelled();
    }

    @Override
    public boolean isDone() {
        return internalFuture.isDone() && synchronisationFuture.isDone();
    }

    @Override
    public T get() throws InterruptedException, ExecutionException {
        T result = internalFuture.get();

        synchronized (SYNCHRONISTION_LOCK) {
            if (!synchronisationComplete && !synchronisationFuture.isDone()) {
                SYNCHRONISTION_LOCK.wait();
                if (!synchronisationComplete) {
                    // synchronisation future was canceled or failed but the internal future not...
                }
            } else {
                // synchronisation future was canceled or failed but the internal future not...
            }
        }

        return result;
    }

    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        T result = internalFuture.get(timeout, unit);

        synchronized (SYNCHRONISTION_LOCK) {
            if (!synchronisationComplete && !synchronisationFuture.isDone()) {
                SYNCHRONISTION_LOCK.wait(TimeUnit.MILLISECONDS.convert(timeout, unit));
                if (!synchronisationComplete && !synchronisationFuture.isDone()) {
                    throw new TimeoutException();
                } else if (!synchronisationComplete) {
                    // synchronisation future was canceled or failed but the internal future not...
                }
            } else {
                // synchronisation future was canceled or failed but the internal future not...
            }
        }

        return result;
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
    protected abstract void addObserver(Observer observer);

    /**
     * Remove the notify change observer from the component whose synchronization is
     * waited for after the synchronization is complete or failed.
     *
     * @param observer In this case always the notify change observer that is added.
     */
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
     * @throws CouldNotPerformException it something goes wrong in the check
     */
    protected abstract boolean check(final T message) throws CouldNotPerformException;
}
