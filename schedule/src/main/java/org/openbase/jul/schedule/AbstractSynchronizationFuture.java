package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * This abstract future can be wrapped around another future to guarantee that changes caused by the internal future
 * have at one time been synchronized to a data provider.
 * To do this a task is started that calls get on the internal future and registers an observer on the data provider.
 * When the data provider notifies a change the method {@link #check(Object)} is called. This method returns true
 * if the data change caused by the internal future has been synchronized. This future only returns on get if this
 * synchronization check finished.
 *
 * @param <T> The return type of the internal future.
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public abstract class AbstractSynchronizationFuture<T, DATA_PROVIDER extends DataProvider<?>> implements Future<T>, FutureWrapper<T> {

    protected final Logger logger;

    private final SyncObject CHECK_LOCK = new SyncObject("WaitForUpdateLock");

    private final Observer notifyChangeObserver = (Object source, Object data) -> {
        synchronized (CHECK_LOCK) {
            CHECK_LOCK.notifyAll();
        }
    };

    private final Future<T> internalFuture;
    private Future synchronisationFuture;

    protected final DATA_PROVIDER dataProvider;

    /**
     * Create a new abstract synchronization future.
     * <p>
     * Note: If the initTask parameter is false the implementation of this future should call {@link #init()} at
     * the end of its constructor. It should be false if you need to initialize more variables inside the constructor
     * which have to be available during the {@link #check(Object)} and {@link #beforeWaitForSynchronization(Object)} methods.
     * Otherwise these values could be null if the internal task is too fast.
     *
     * @param internalFuture the internal future
     * @param dataProvider   the data provider
     * @param initTask       value indicating if the internal task should already be started
     */
    public AbstractSynchronizationFuture(final Future<T> internalFuture, final DATA_PROVIDER dataProvider, final boolean initTask) {
        this.logger = LoggerFactory.getLogger(dataProvider.getClass());
        this.internalFuture = internalFuture;
        this.dataProvider = dataProvider;

        if (initTask) {
            init();
        }
    }

    /**
     * Start the internal synchronization task.
     */
    protected void init() {
        // create a synchronisation task which makes sure that the change requested by
        // the internal future has at one time been synchronized to the remote
        synchronisationFuture = GlobalCachedExecutorService.submit(() -> {
            dataProvider.addDataObserver(notifyChangeObserver);
            try {
                dataProvider.waitForData();
                T result = internalFuture.get();
                waitForSynchronization(result);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not sync with internal future!", ex, logger);
            } finally {
                dataProvider.removeDataObserver(notifyChangeObserver);
            }
            return null;
        });
    }

    private void validateInitialization() throws InvalidStateException {
        if (synchronisationFuture == null) {
            throw new InvalidStateException(this + " not initialized!");
        }
    }

    private boolean isInitialized() {
        try {
            validateInitialization();
        } catch (InvalidStateException ex) {
            ExceptionPrinter.printHistory(new FatalImplementationErrorException(this, ex), logger);
            return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     * @param mayInterruptIfRunning {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        if (!isInitialized()) {
            return internalFuture.cancel(mayInterruptIfRunning);
        }
        return synchronisationFuture.cancel(mayInterruptIfRunning) && internalFuture.cancel(mayInterruptIfRunning);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isCancelled() {
        if (!isInitialized()) {
            return internalFuture.isCancelled();
        }
        return synchronisationFuture.isCancelled() && internalFuture.isCancelled();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public boolean isDone() {
        if (!isInitialized()) {
            return internalFuture.isDone();
        }
        return synchronisationFuture.isDone() && internalFuture.isDone();
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     */
    @Override
    public T get() throws InterruptedException, ExecutionException {
        // when get returns without an exception the synchronisation is complete
        // and else the exception will be thrown
        if (isInitialized()) {
            synchronisationFuture.get();
        }
        return internalFuture.get();
    }

    /**
     * {@inheritDoc}
     * @param timeout {@inheritDoc}
     * @param unit {@inheritDoc}
     * @return {@inheritDoc}
     * @throws InterruptedException {@inheritDoc}
     * @throws ExecutionException {@inheritDoc}
     * @throws TimeoutException {@inheritDoc}
     */
    @Override
    public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // when get returns without an exception the synchronisation is complete
        // and else the exception will be thrown
        if (isInitialized()) {
            synchronisationFuture.get(timeout, unit);
        }
        return internalFuture.get(timeout, unit);
    }

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     */
    @Override
    public Future<T> getInternalFuture() {
        return internalFuture;
    }

    private void waitForSynchronization(T message) throws CouldNotPerformException, InterruptedException {
        try {
            try {
                beforeWaitForSynchronization(message);
            } catch (final Exception ex) {
                throw new CouldNotPerformException("Pre execution task failed!", ex);
            }

            synchronized (CHECK_LOCK) {
                while (!check(message)) {
                    CHECK_LOCK.wait();
                }
            }
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not wait for synchronization!", ex);
        }
    }

    /**
     * Called before the synchronization task enters its loop. Can for example
     * be used to wait for initial data so that the check that is done afterwards
     * in the loop does not fail immediately.
     * <p>
     * Note: Method can be overwritten for custom pre synchronization actions.
     *
     * @throws CouldNotPerformException if something goes wrong
     */
    protected void beforeWaitForSynchronization(final T message) throws CouldNotPerformException {
        // Method can be overwritten for custom pre synchronization actions.
    }

    /**
     * Called inside of the synchronization loop to check if the synchronization is complete.
     *
     * @param message the return value of the internal future
     * @return true if the synchronization is complete and else false
     * @throws CouldNotPerformException if something goes wrong in the check
     */
    protected abstract boolean check(final T message) throws CouldNotPerformException;
}
