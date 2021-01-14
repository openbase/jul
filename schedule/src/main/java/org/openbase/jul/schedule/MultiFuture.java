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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * A MultiFuture can be used to aggregate a list of futures.
 * <p>
 * The MultiFuture represents the state of all subfutures.
 *
 * @param <FUTURE_TYPE> the result type of the subfutures.
 */
public class MultiFuture<FUTURE_TYPE> implements Future<List<FUTURE_TYPE>> {

    public enum AggregationStrategy {
        ANY_OF,
        ALL_OF
    }

    protected final List<Future<FUTURE_TYPE>> futureList;

    public MultiFuture(final Collection<Future<FUTURE_TYPE>> futureCollection) {
        this.futureList = new ArrayList<>(futureCollection);
    }

    /**
     * Cancels all internal futures.
     *
     * @param mayInterruptIfRunning defines if waiting threads should be interrupted.
     *
     * @return true if all subfutures are successfully canceled.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        boolean success = true;
        for (Future<FUTURE_TYPE> future : futureList) {
            success &= future.cancel(true);
        }
        return success;
    }

    /**
     * Method returns true if at least one subfutures was canceled. In case there are still processing futures
     * remaining, than false is returned in any case. False is returned as well when all futures are done but none was canceled.
     *
     * @return see method description.
     */
    @Override
    public boolean isCancelled() {
        boolean canceled = false;
        for (Future<FUTURE_TYPE> future : futureList) {
            if (!future.isDone()) {
                return false;
            }
            canceled |= future.isCancelled();
        }
        return canceled;
    }

    /**
     * Method checks if the subfutures are done.
     *
     * @return true if all futures are done, otherwise false.
     */
    @Override
    public boolean isDone() {
        for (Future<FUTURE_TYPE> future : futureList) {
            if (!future.isDone()) {
                return false;
            }
        }
        return true;
    }

    /**
     * Blocks until all subfuture results are available.
     *
     * @return a list of future results.
     *
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException   in thrown if the execution of at least one subfuture is not available.
     */
    @Override
    public List<FUTURE_TYPE> get() throws InterruptedException, ExecutionException {
        final List<FUTURE_TYPE> resultList = new ArrayList<>();
        for (Future<FUTURE_TYPE> future : futureList) {
            resultList.add(future.get());
        }
        return resultList;
    }

    /**
     * Blocks until all subfuture results are available.
     *
     * @param timeout  the time until a timeout is thrown.
     * @param timeUnit the unit of the timeout declaration.
     *
     * @return a list of future results.
     *
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException   in thrown if the execution of at least one subfuture is not available.
     * @throws TimeoutException     is thrown if the timeout is reached.
     */
    @Override
    public List<FUTURE_TYPE> get(long timeout, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        final TimeoutSplitter timeoutSplitter = new TimeoutSplitter(timeout, timeUnit);
        final List<FUTURE_TYPE> resultList = new ArrayList<>();
        for (Future<FUTURE_TYPE> future : futureList) {
            try {
                resultList.add(future.get(timeoutSplitter.getTime(), timeoutSplitter.getTimeUnit()));
            } catch (org.openbase.jul.exception.TimeoutException ex) {
                throw new TimeoutException();
            }
        }
        return resultList;
    }

    /**
     * Returns all subfutures of this multi future.
     * @return a list of subfutures.
     */
    public List<Future<FUTURE_TYPE>> getFutureList() {
        return futureList;
    }
}
