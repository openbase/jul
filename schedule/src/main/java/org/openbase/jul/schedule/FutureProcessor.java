package org.openbase.jul.schedule;

/*
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * A collection of useful methods to generate and process future objects.
 */
public class FutureProcessor {

    /**
     * Method transforms a callable into a CompletableFuture object.
     *
     * @param <T>
     * @param callable the callable to wrap.
     * @return the Future representing the call state.
     */
    public static <T> CompletableFuture<T> toCompletableFuture(final Callable<T> callable) {
        CompletableFuture<T> future = new CompletableFuture<>();
        CompletableFuture.runAsync(() -> {
            try {
                future.complete(callable.call());
            } catch (InterruptedException ex) {
                future.completeExceptionally(ex);
                Thread.currentThread().interrupt();
            } catch (Exception ex) {
                future.completeExceptionally(ex);
            }
        });
        return future;
    }

    /**
     * Method returns a future which is already canceled by the given cause.
     *
     * @param <T> the type of the future
     * @param futureType the type class of the future
     * @param cause the reason why the future was canceled.
     * @return the canceled future.
     */
    public static <T> Future<T> canceledFuture(final Class<T> futureType, final Exception cause) {
        return new Future<T>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return true;
            }

            @Override
            public boolean isCancelled() {
                return true;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public T get() throws InterruptedException, ExecutionException {
                throw new ExecutionException(cause);
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new ExecutionException(cause);
            }
        };
    }

    /**
     * Generates an already completed future instance.
     * @param value the value used for the completion.
     * @param <VALUE> the value type of the future.
     * @return the future object completed with the given {@code value}.
     */
    public static <VALUE> Future<VALUE> completedFuture(final VALUE value) {
        return new Future<VALUE>() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return false;
            }

            @Override
            public boolean isCancelled() {
                return false;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public VALUE get() {
                return value;
            }

            @Override
            public VALUE get(long timeout, TimeUnit unit) {
                return value;
            }
        };
    }

    /**
     * A Void future prototype.
     */
    private static final Future<Void> COMPLETED_VOID_FUTURE_PROTOTYPE = new Future<Void>() {
        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            return false;
        }

        @Override
        public boolean isCancelled() {
            return false;
        }

        @Override
        public boolean isDone() {
            return true;
        }

        @Override
        public Void get() {
            return null;
        }

        @Override
        public Void get(long timeout, TimeUnit unit) {
            return null;
        }
    };

    /**
     * Returns an already completed future instance.
     * @return the future object completed with the a null value.
     */
    public static Future<Void> completedFuture() {
        return COMPLETED_VOID_FUTURE_PROTOTYPE;
    }
    
    /**
     * Method returns a future which is already canceled by the given cause.
     *
     * @param cause the reason why the future was canceled.
     * @return the canceled future.
     */
    public static Future canceledFuture(final Exception cause) {
        return new Future() {
            @Override
            public boolean cancel(boolean mayInterruptIfRunning) {
                return true;
            }

            @Override
            public boolean isCancelled() {
                return true;
            }

            @Override
            public boolean isDone() {
                return true;
            }

            @Override
            public Object get() throws InterruptedException, ExecutionException {
                throw new ExecutionException(cause);
            }

            @Override
            public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
                throw new ExecutionException(cause);
            }
        };
    }
}
