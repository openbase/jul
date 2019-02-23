package org.openbase.jul.pattern;

/*-
 * #%L
 * JUL Pattern Default
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

import org.openbase.jul.exception.FatalImplementationErrorException;

import java.util.concurrent.*;

/**
 * This is a lite version of the jdk8 CompletableFuture implementation. It was implemented to be used on android 6 which does not support the default implementation.
 * However, this lite implementation only enables to complete and to cancel the future.
 *
 * @param <V> the type of result value.
 */
public class CompletableFutureLite<V> implements Future<V> {

    private final Object lock = new Object() {
        @Override
        public String toString() {
            return "CompletableFutureLite.Lock";
        }
    };

    /**
     * This is the value were the result is stored in case the future is complete.
     */
    private V value = null;

    /**
     * This is the variable to store an exception in case the future was completed exceptionally.
     */
    private Throwable throwable = null;

    /**
     * This method enables to complete the future with the given {@code value}.
     * It will be nothing changed if the future was already completed or canceled in advance.
     *
     * @param value the value used to complete the future.
     *
     * @return if this invocation caused this CompletableFuture to be done, than {@code true} is returned. Otherwise {@code false} is returned.
     */
    public boolean complete(V value) {
        synchronized (lock) {
            if (isDone()) {
                return false;
            }
            this.value = value;
            lock.notifyAll();
        }
        return true;
    }

    /**
     * This method enables to exceptionally complete the future with the given {@code throwable}.
     * It will be nothing changed if the future was already completed or canceled in advance.
     *
     * @param throwable the throwable used to exceptionally complete the future.
     *
     * @return if this invocation caused this CompletableFuture to be done, than {@code true} is returned. Otherwise {@code false} is returned.
     */
    public boolean completeExceptionally(Throwable throwable) {
        synchronized (lock) {
            if (isDone()) {
                return false;
            }
            this.throwable = throwable;
            lock.notifyAll();
            return true;
        }
    }

    /**
     * Method cancels the future without delivering any specific reason.
     *
     * @param mayInterruptIfRunning this flag is ignored since this implementation does not use its own computation thread.
     *
     * @return if this invocation caused this CompletableFuture to be canceled, than {@code true} is returned. Otherwise {@code false} is returned.
     */
    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        synchronized (lock) {
            if (isDone()) {
                return false;
            }
            throwable = new CancellationException();
            lock.notifyAll();
            return true;
        }
    }


    /**
     * Method returns if this future was canceled. This does not include a exceptional completed future, because its not canceled.
     *
     * @return {@code true} if this future was canceled, else {@code false}.
     */
    @Override
    public boolean isCancelled() {
        return throwable != null && throwable instanceof CancellationException;
    }

    /**
     * Method returns if this future is still in progress or if it is already done. Done means the future was completed, exceptionally completed or canceled.
     *
     * @return {@code true} if future is completed or exceptionally completed or canceled
     */
    @Override
    public boolean isDone() {
        return value != null || throwable != null;
    }

    /**
     * Method returns if the task has failed.
     *
     * @return {@code true} if exceptionally completed, otherwise {@code false}.
     */
    public boolean isFailed() {
        return throwable != null && !isCancelled();
    }

    /**
     * Method blocks until the future is done. The method return the result if the future was normally completed.
     * If the future was exceptionally completed or canceled an exception is thrown.
     *
     * @return the result of the task.
     *
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException   is thrown if the task was canceled or exceptionally completed.
     */
    @Override
    public V get() throws InterruptedException, ExecutionException {
        synchronized (lock) {
            if (value != null) {
                return value;
            }
            lock.wait();

            if (value != null) {
                return value;
            }

            if (throwable != null) {
                throw new ExecutionException(throwable);
            }
            throw new ExecutionException(new FatalImplementationErrorException("Not terminated after notification!", this));
        }
    }

    /**
     * @return the result of the task.
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException is thrown if the task was canceled or exceptionally completed.
     */

    /**
     * Method blocks until the future is done. The method return the result if the future was normally completed.
     * If the future was exceptionally completed or canceled an exception is thrown.
     *
     * @param timeout  the maximal time to wait for completion.
     * @param timeUnit the unit of the given timeout.
     *
     * @return the result of the task.
     *
     * @throws InterruptedException is thrown if the thread was externally interrupted.
     * @throws ExecutionException   is thrown if the task was canceled or exceptionally completed.
     * @throws TimeoutException     in thrown if the timeout was reached and the task is still not done.
     */
    @Override
    public V get(final long timeout, final TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
        synchronized (lock) {
            if (value != null) {
                return value;
            }
            lock.wait(timeUnit.toMillis(timeout));

            if (value != null) {
                return value;
            }

            if (throwable != null) {
                throw new ExecutionException(throwable);
            }
            throw new TimeoutException();
        }
    }
}
