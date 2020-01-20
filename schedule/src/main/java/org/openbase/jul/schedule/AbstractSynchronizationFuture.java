package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import org.openbase.jul.exception.ExceptionProcessor;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.TimedProcessable;
import org.openbase.jul.pattern.CompletableFutureLite;
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
 * To do this an observer is registered on the data provider.
 * When the data provider notifies a change the method {@link #check(Object)} is called. This method returns true
 * if the data change caused by the internal future has been synchronized. This future only returns on get if this
 * synchronization check finished.
 *
 * @param <T> The return type of the internal future.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public abstract class AbstractSynchronizationFuture<T, DATA_PROVIDER extends DataProvider<?>> extends ResultProcessingFuture<T, T> {

    protected final Logger logger;

    private final SyncObject CHECK_LOCK = new SyncObject("WaitForUpdateLock");

    protected final DATA_PROVIDER dataProvider;

    protected final TimedProcessable resultProcessor;

    /**
     * Create a new abstract synchronization future.
     *
     * @param internalFuture the internal future
     * @param dataProvider   the data provider
     */
    public AbstractSynchronizationFuture(final Future<T> internalFuture, final DATA_PROVIDER dataProvider) {

        super(internalFuture);
        this.logger = LoggerFactory.getLogger(dataProvider.getClass());
        this.dataProvider = dataProvider;

        this.resultProcessor = (TimedProcessable<T, T>) (input, timeout, timeUnit) -> performInternalSync(input, timeout, timeUnit);
        this.init(resultProcessor);
    }

    private T performInternalSync(final T input, final long timeout, final TimeUnit timeUnit) throws InterruptedException, CouldNotPerformException, TimeoutException {
        final Observer notifyChangeObserver = (Object source, Object data) -> {
            synchronized (CHECK_LOCK) {
                CHECK_LOCK.notifyAll();
            }
        };

        dataProvider.addDataObserver(notifyChangeObserver);
        try {
            // todo split timeout
            dataProvider.waitForData(timeout, timeUnit);
            // todo split timeout
            final T result = getInternalFuture().get(timeout, timeUnit);
            return waitForSynchronization(result, timeout, timeUnit);
        } catch (CouldNotPerformException | ExecutionException ex) {
            if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                ExceptionPrinter.printHistory("Could not sync with internal future!", ex, logger);
            }

            // handle timeout exception
            final Throwable initialCause = ExceptionProcessor.getInitialCause(ex);
            if (initialCause instanceof TimeoutException || initialCause instanceof org.openbase.jul.exception.TimeoutException) {
                throw new TimeoutException();
            }

            throw new CouldNotPerformException("Could not validate future synchronisation!", ex);
        } finally {
            dataProvider.removeDataObserver(notifyChangeObserver);
        }
    }

    private T waitForSynchronization(final T message, final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException, TimeoutException {
        try {
            try {
                beforeWaitForSynchronization(message);
            } catch (final Exception ex) {
                throw new CouldNotPerformException("Pre execution task failed!", ex);
            }

            long endTimestamp = System.currentTimeMillis() + timeUnit.toMillis(timeout);

            synchronized (CHECK_LOCK) {
                while (!check(message)) {

                    if (getInternalFuture().isCancelled() || isCancelled()) {
                        throw new InvalidStateException("Future was canceled!");
                    }

                    if(endTimestamp <= System.currentTimeMillis()) {
                        throw new TimeoutException();
                    }

                    // timeout used as fallback in case the observation task is not properly implemented.
                    CHECK_LOCK.wait(Math.max(0, Math.min(2000, endTimestamp - System.currentTimeMillis())));
                }
            }

            return message;
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
     *
     * @return true if the synchronization is complete and else false
     *
     * @throws CouldNotPerformException if something goes wrong in the check
     */
    protected abstract boolean check(final T message) throws CouldNotPerformException;
}
