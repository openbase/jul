package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import lombok.NonNull;
import org.openbase.jul.exception.*;
import org.openbase.jul.iface.Initializable;
import org.openbase.jul.iface.TimedProcessable;
import org.openbase.jul.pattern.CompletableFutureLite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A result processing future.
 *
 * @param <I> The output or result type of the futures provided by the future collection.
 * @param <R> The result type of the future.
 */

public class ResultProcessingFuture<I, R> extends CompletableFutureLite<R> implements Initializable<TimedProcessable<I, R>>, FutureWrapper<I>{

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultProcessingMultiFuture.class);

    private final ReentrantReadWriteLock updateComponentLock = new ReentrantReadWriteLock();

    private @NonNull TimedProcessable<I, R> resultProcessor;
    private @NonNull final Future<I> internalFuture;

    public ResultProcessingFuture(@NonNull final TimedProcessable<I, R> resultProcessor, @NonNull final Future<I> internalFuture) {
        this.internalFuture = internalFuture;
        this.init(resultProcessor);
    }

    protected ResultProcessingFuture(@NonNull final Future<I> internalFuture) {
        this.internalFuture = internalFuture;
    }

    @Override
    public void init(@NonNull final TimedProcessable<I, R> resultProcessor) {
        this.resultProcessor = resultProcessor;
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {

        internalFuture.cancel(true);
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {

        if (isDone()) {
            return super.get();
        }

        updateComponentLock.writeLock().lockInterruptibly();
        try {
            // this is important because in the mean time the task can be done.
            if (isDone()) {
                return super.get();
            }

            // validate initialization
            if(resultProcessor == null) {
                completeExceptionally(new FatalImplementationErrorException(this, new InvalidStateException("Not Initialized")));
            }

            // handle completion
            try {
                complete(resultProcessor.process(internalFuture.get()));
            } catch (CouldNotPerformException | ExecutionException | CancellationException ex) {
                completeExceptionally(ex);
            } catch (InterruptedException ex) {
                throw ex;
            } catch (Exception ex) {
                completeExceptionally(new FatalImplementationErrorException(this, ex));
            }
        } finally {
            updateComponentLock.writeLock().unlock();
        }

        return super.get();
    }

    @Override
    public R get(final long timeout, final TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
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

            // validate initialization
            if(resultProcessor == null) {
                completeExceptionally(new FatalImplementationErrorException(this, new InvalidStateException("Not Initialized")));
            }

            // handle completion
            try {
                // todo implement time split
                complete(resultProcessor.process(internalFuture.get(timeout, unit), timeout, unit));
            } catch (CouldNotPerformException | ExecutionException | CancellationException ex) {
                completeExceptionally(ex);
            } catch (InterruptedException | TimeoutException ex) {
                throw ex;
            } catch (Exception ex) {
                completeExceptionally(new FatalImplementationErrorException(this, ex));
            }
        } finally {
            updateComponentLock.writeLock().unlock();
        }

        return super.get(timeout, unit);
    }

    @Override
    public Future<I> getInternalFuture() {
        return internalFuture;
    }
}
