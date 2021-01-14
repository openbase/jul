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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.iface.TimedProcessable;
import org.openbase.jul.pattern.CompletableFutureLite;
import org.openbase.jul.schedule.MultiFuture.AggregationStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * A result processing multi future.
 *
 * @param <O> The output of the futures provided by the future collection.
 * @param <R> The result type of the future.
 */
public class ResultProcessingMultiFuture<O, R> extends CompletableFutureLite<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultProcessingMultiFuture.class);

    private final ReentrantReadWriteLock updateComponentLock = new ReentrantReadWriteLock();

    private final TimedProcessable<Collection<Future<O>>, R> resultProcessor;
    private final MultiFuture<O> multiFuture;

    public ResultProcessingMultiFuture(final TimedProcessable<Collection<Future<O>>, R> resultProcessor, final Collection<Future<O>> futureList) {
        this(resultProcessor, futureList, AggregationStrategy.ALL_OF);
    }

    public ResultProcessingMultiFuture(final TimedProcessable<Collection<Future<O>>, R> resultProcessor, final Collection<Future<O>> futureList, final AggregationStrategy aggregationStrategy) {
        this.resultProcessor = resultProcessor;

        // initiate multi future related to aggregation strategy
        switch (aggregationStrategy) {
            case ANY_OF:
                this.multiFuture = new AnyOfMultiFuture<>(futureList);
                break;
            case ALL_OF:
            default:
                this.multiFuture = new MultiFuture<>(futureList);
        }
    }

    @Override
    public boolean cancel(final boolean mayInterruptIfRunning) {

        // cancel multi future.
        multiFuture.cancel(true);
        return super.cancel(mayInterruptIfRunning);
    }

    @Override
    public R get() throws InterruptedException, ExecutionException {
        try {
            return get(Timeout.INFINITY_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            throw new ExecutionException(new FatalImplementationErrorException("Timeout exception occurred on infinity timeout!", this, ex));
        }
    }

    @Override
    public R get(final long timeout, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        final TimeoutSplitter timeoutSplitter = new TimeoutSplitter(timeout, timeUnit);
        try {
            if (isDone()) {
                return super.get(timeoutSplitter.getTime(), timeoutSplitter.getTimeUnit());
            }

            if (!updateComponentLock.writeLock().tryLock(timeoutSplitter.getTime(), timeoutSplitter.getTimeUnit())) {
                throw new TimeoutException();
            }
            try {
                // this is important because in the mean time the task can be done.
                if (isDone()) {
                    return super.get(timeoutSplitter.getTime(), timeoutSplitter.getTimeUnit());
                }

                // handle completion
                try {
                    multiFuture.get(timeoutSplitter.getTime(), timeoutSplitter.getTimeUnit());
                    complete(resultProcessor.process(multiFuture.getFutureList()));
                } catch (InterruptedException | TimeoutException ex) {
                    throw ex;
                } catch (CouldNotPerformException | ExecutionException | CancellationException ex) {
                    completeExceptionally(ex);
                } catch (Exception ex) {
                    completeExceptionally(new FatalImplementationErrorException(this, ex));
                }
            } finally {
                updateComponentLock.writeLock().unlock();
            }

            return super.get(timeoutSplitter.getTime(), timeoutSplitter.getTimeUnit());

        } catch (org.openbase.jul.exception.TimeoutException ex) {
            throw new TimeoutException();
        }
    }
}
