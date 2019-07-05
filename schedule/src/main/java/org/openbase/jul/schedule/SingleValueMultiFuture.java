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
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Processable;
import org.openbase.jul.pattern.CompletableFutureLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A result processing multi future.
 *
 * @param <R> The result type of the future.
 */
public class SingleValueMultiFuture<R> extends CompletableFutureLite<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SingleValueMultiFuture.class);

    private final ReentrantReadWriteLock updateComponentLock = new ReentrantReadWriteLock();

    final Callable<R> resultCallable;
    private MultiFuture<?> multiFuture;


    public SingleValueMultiFuture(final Callable<R> resultCallable, final Collection<? extends Future<?>> futureList) {
        this.multiFuture = new MultiFuture(futureList);
        this.resultCallable = resultCallable;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {

        // cancel multi future.
        multiFuture.cancel(true);
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {

        LOGGER.error("JOJO get");

        if (isDone()) {
            return super.get();
        }

        updateComponentLock.writeLock().lock();
        try {
            // this is important because in the mean time the task can be done.
            if (isDone()) {
                return super.get();
            }

            // handle completion
            try {
                multiFuture.get();
                complete(resultCallable.call());
            } catch (CouldNotPerformException | InterruptedException | CancellationException ex) {
                completeExceptionally(ex);
            } catch (Exception ex) {
                completeExceptionally(ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Task execution failed!", ex), LOGGER));
            }
        } finally {
            updateComponentLock.writeLock().unlock();
        }

        return super.get();
    }

    @Override
    public R get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {

        LOGGER.error("JOJO get time");

        if (isDone()) {
            return super.get(timeout, unit);
        }

        if (!updateComponentLock.writeLock().tryLock(timeout, unit)) {
            throw new TimeoutException();
        }
        try {

            // this is important because in the mean time the task can be done.
            if (isDone()) {
                return super.get(timeout, unit);
            }

            // handle completion
            try {
                multiFuture.get(timeout, unit);
                complete(resultCallable.call());
            } catch (CouldNotPerformException | InterruptedException | CancellationException ex) {
                completeExceptionally(ex);
            } catch (Exception ex) {
                completeExceptionally(ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Task execution failed!", ex), LOGGER));
            }
        } finally {
            updateComponentLock.writeLock().unlock();
        }

        return super.get();
    }
}
