package org.openbase.jul.schedule;

import java.util.ArrayList;
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

    private final List<Future<FUTURE_TYPE>> futureList;

    public MultiFuture(final List<Future<FUTURE_TYPE>> futureList) {
        this.futureList = futureList;
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
        // todo implement timestamp split.
        final List<FUTURE_TYPE> resultList = new ArrayList<>();
        for (Future<FUTURE_TYPE> future : futureList) {
            resultList.add(future.get(timeout, timeUnit));
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
