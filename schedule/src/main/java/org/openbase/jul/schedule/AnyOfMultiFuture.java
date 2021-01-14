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

import org.openbase.jul.exception.FatalImplementationErrorException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.*;

/**
 * A OneOfMultiFuture can be used to aggregate a list of futures.
 * <p>
 * The OneOfMultiFuture represents the state of all futures which are already done, otherwise the OneOfMultiFuture is not done yet.
 *
 * @param <FUTURE_TYPE> the result type of the subfutures.
 */
public class AnyOfMultiFuture<FUTURE_TYPE> extends MultiFuture<FUTURE_TYPE> {

    public AnyOfMultiFuture(final Collection<Future<FUTURE_TYPE>> futureCollection) {
        super(futureCollection);
    }

    /**
     * Method checks if the subfutures are done.
     *
     * @return true if at least one future is done, otherwise false.
     */
    @Override
    public boolean isDone() {
        for (Future<FUTURE_TYPE> future : futureList) {
            if (future.isDone()) {
                return true;
            }
        }
        return false;
    }

    /**
     * Blocks until at least one subfuture result is available.
     *
     * @return a list of available future results.
     *
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException   in thrown if the execution of at least one subfuture is not available.
     */
    @Override
    public List<FUTURE_TYPE> get() throws InterruptedException, ExecutionException {
        try {
            return get(Timeout.INFINITY_TIMEOUT, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ex) {
            throw new ExecutionException(new FatalImplementationErrorException("Timeout exception occurred on infinity timeout!", this, ex));
        }
    }

    /**
     * Blocks until at least one subfuture result is available.
     *
     * @param timeout  the time until a timeout is thrown.
     * @param timeUnit the unit of the timeout declaration.
     *
     * @return a list of available future results.
     *
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException   in thrown if the execution of at least one subfuture is not available.
     * @throws TimeoutException     is thrown if the timeout is reached.
     */
    @Override
    public List<FUTURE_TYPE> get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        final TimeoutSplitter timeoutSplitter = new TimeoutSplitter(timeout, timeUnit);
        final List<FUTURE_TYPE> resultList = new ArrayList<>();

        // check if at least one future is finished.
        for (Future<FUTURE_TYPE> future : futureList) {
            if(future.isDone() && !future.isCancelled()) {
                resultList.add(future.get());
            }
        }

        // return if at least on future is ready.
        if(!resultList.isEmpty()) {
            return resultList;
        }

        // block until at least one future is available
        while (true) {

            // validate interruption
            if(Thread.currentThread().isInterrupted()) {
                throw new InterruptedException();
            }

            // validate cancellation
            if(isCancelled()) {
                throw new CancellationException();
            }

            // check if first result is available
            for (Future<FUTURE_TYPE> future : futureList) {

                // if result is available then collect all results via recursion.
                if(future.isDone() && !future.isCancelled()) {
                    return get();
                }
            }

            // wait max 10 ms until next check iteration
            try {
                Thread.sleep(Math.min(10l, timeoutSplitter.getTime()));
            } catch (org.openbase.jul.exception.TimeoutException ex) {
                throw new TimeoutException();
            }
        }
    }
}
