package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Processable;
import org.openbase.jul.iface.TimedProcessable;
import org.openbase.jul.schedule.MultiFuture.AggregationStrategy;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.TimeoutException;

import static org.openbase.jul.schedule.GlobalScheduledExecutorService.getInstance;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * <p>
 * A collection of useful methods to generate and process future objects.
 */
public class FutureProcessor {

    /**
     * Method transforms a callable into a CompletableFuture object.
     *
     * @param <T>
     * @param callable the callable to wrap.
     *
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
     * Note: In case an interrupted exception is passed as cause,
     * the interruption is handled by interrupting the current thread.
     *
     * @param <T>        the type of the future
     * @param futureType the type class of the future
     * @param cause      the reason why the future was canceled.
     *
     * @return the canceled future.
     */
    public static <T> Future<T> canceledFuture(final Class<T> futureType, final Exception cause) {

        // handle interruption before encapsulating an interrupted exception.
        if(cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }

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
            public T get() throws ExecutionException {
                throw new ExecutionException(cause);
            }

            @Override
            public T get(long timeout, TimeUnit unit) throws ExecutionException {
                throw new ExecutionException(cause);
            }
        };
    }

    /**
     * Generates an already completed future instance.
     *
     * @param value   the value used for the completion.
     * @param <VALUE> the value type of the future.
     *
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
     *
     * @return the future object completed with the a null value.
     */
    public static Future<Void> completedFuture() {
        return COMPLETED_VOID_FUTURE_PROTOTYPE;
    }

    /**
     * Method returns a future which is already canceled by the given cause.
     *
     * @param cause the reason why the future was canceled.
     *
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

    /**
     * This method applies an error handler to the given future object.
     * In case the given timeout is expired or the future processing fails the error processor is processed with the occurred exception as argument.
     * The receive a future should be submitted to any execution service or handled externally.
     *
     * @param future          the future on which is the error processor is registered.
     * @param timeout         the timeout.
     * @param errorProcessor  the processable which handles thrown exceptions
     * @param timeUnit        the unit of the timeout.
     * @param executorService the execution service to apply the handler.
     *
     * @return the future of the error handler.
     */
    public static Future applyErrorHandling(final Future<?> future, final Processable<Exception, Void> errorProcessor, final long timeout, final TimeUnit timeUnit, final ExecutorService executorService) {
        return executorService.submit(() -> {
            try {
                future.get(timeout, timeUnit);
            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                errorProcessor.process(ex);
            }
            return null;
        });
    }

    /**
     * This method applies an error handler to the given future object.
     * In case the given timeout is expired or the future processing fails the error processor is processed with the occurred exception as argument.
     * The receive a future should be submitted to any execution service or handled externally.
     *
     * @param future         the future on which is the error processor is registered.
     * @param timeout        the timeout.
     * @param errorProcessor the processable which handles thrown exceptions
     * @param timeUnit       the unit of the timeout.
     *
     * @return the future of the error handler.
     */
    public static Future applyErrorHandling(final Future future, final Processable<Exception, Void> errorProcessor, final long timeout, final TimeUnit timeUnit) {
        return applyErrorHandling(future, errorProcessor, timeout, timeUnit, getInstance().getExecutorService());
    }

    public static <I, R> Future<Void> allOf(final Collection<I> inputList, final TimedProcessable<I, Future<R>> taskProcessor) {
        return allOf(inputList, (input, time, timeUnit) -> null, taskProcessor);
    }

    public static Future<Void> allOf(final Future<?>... futures) {
        return allOf(Arrays.asList(futures));
    }

    public static <R> Future<R> allOf(final Callable resultCallable, final Future<?>... futures) {
        return allOf(resultCallable, Arrays.asList(futures));
    }

    public static <R> Future<R> allOfInclusiveResultFuture(final Future<R> resultFuture, final Future<?>... futures) {
        final List<Future<?>> futureList = new ArrayList<>(Arrays.asList(futures));

        // Add result future to future list to make sure that the result is available if requested.
        futureList.add(resultFuture);

        return allOf(() -> resultFuture.get(), futureList);
    }

    public static <R> Future<R> allOf(final Collection<? extends Future<?>> futureCollection) {
        return allOf(() -> null, futureCollection);
    }

    public static <R> Future<R> allOf(final R returnValue, final Collection<? extends Future<?>> futureCollection) {
        return allOf(returnValue, futureCollection);
    }

    public static <I, O, R> Future<R> allOf(final Collection<I> inputList, final TimedProcessable<Collection<Future<O>>, R> resultProcessor, final TimedProcessable<I, Future<O>> taskProcessor) {
        try {
            return new ResultProcessingMultiFuture<>(resultProcessor, buildFutureCollection(inputList, taskProcessor));
        } catch (CouldNotPerformException ex) {
            return (Future<R>) FutureProcessor.canceledFuture(ex);
        }
    }

    public static <I, R> Future<R> postProcess(@NonNull final TimedProcessable<I, R> resultProcessor, @NonNull final Future<I> future) {
        return new ResultProcessingFuture<I, R>(resultProcessor, future);
    }

    /**
     * Method generates a new futures which represents all futures provided by the futureCollection. If all futures are successfully finished the outer future will be completed with the result provided by the resultCallable.
     *
     * @param <R>              The result type of the outer future.
     * @param resultCallable   the callable which provides the result of the outer future.
     * @param futureCollection the inner future collection.
     *
     * @return the outer future.
     */
    public static <R> Future<R> allOf(final Callable<R> resultCallable, final Collection<? extends Future<?>> futureCollection) {

        if (resultCallable == null) {
            return (Future<R>) FutureProcessor.canceledFuture(new NotAvailableException("resultCallable"));
        }

        return new SingleValueMultiFuture<>(resultCallable, futureCollection);
    }

    /**
     * Method generates a new futures which represents all futures provided by the futureCollection. If all futures are successfully finished the outer future will be completed with the result provided by the resultProcessor.
     * <p>
     * Node: For this method it's important that all futures provided by the future collection provide the same result type.
     *
     * @param <O>              The output or result type of the futures provided by the future collection.
     * @param <R>              The result type of the outer future.
     * @param resultProcessor  the processor which provides the outer future result.
     * @param futureCollection the inner future collection.
     *
     * @return the outer future.
     */
    public static <O, R> Future<R> allOf(final TimedProcessable<Collection<Future<O>>, R> resultProcessor, final Collection<Future<O>> futureCollection) {
        return new ResultProcessingMultiFuture<>(resultProcessor, futureCollection);
    }

    /**
     * Method generates a new futures which represents all futures provided by the futureCollection.
     * If at least one future successfully finishes the outer future will be completed with the {@code resultValue}.
     *
     * @param <R>              The result type of the outer future.
     * @param <O>              The future type of the sub futures.
     * @param returnValue      Value is used to complete the outer future when at least one task is done.
     * @param futureCollection the inner future collection.
     *
     * @return the outer future.
     */
    public static <O, R> Future<R> anyOf(final R returnValue, final Collection<Future<O>> futureCollection) {
        return anyOf((input, timeout, timeUnit) -> returnValue, futureCollection);
    }

    /**
     * Method generates a new futures which represents all futures provided by the futureCollection.
     * If at least one future successfully finishes the outer future will be completed with the result provided by the resultCallable.
     *
     * @param <R>              The result type of the outer future.
     * @param <O>              The future type of the sub futures.
     * @param resultProcessor  the callable which provides the result of the outer future.
     * @param futureCollection the inner future collection.
     *
     * @return the outer future.
     */
    public static <O, R> Future<R> anyOf(final TimedProcessable<Collection<Future<O>>, R> resultProcessor, final Collection<Future<O>> futureCollection) {
        return new ResultProcessingMultiFuture<O, R>(resultProcessor, futureCollection, AggregationStrategy.ANY_OF);
    }

    /**
     * Method builds a future collection with the given task processor. The input list is passed to the future build process so the input is available during the build.
     *
     * @param <I>           The type of the input value used for the future build.
     * @param <O>           The type of the output value which the futures provide.
     * @param inputList     the input list which is needed for the build process.
     * @param taskProcessor the task processor to build the futures.
     *
     * @return the collection of all builded future instances.
     *
     * @throws CouldNotPerformException is thrown if the future collection could not be generated.
     */
    public static <I, O> Collection<Future<O>> buildFutureCollection(final Collection<I> inputList, final Processable<I, Future<O>> taskProcessor) throws CouldNotPerformException {
        try {
            MultiException.ExceptionStack exceptionStack = null;
            final List<Future<O>> futureList = new ArrayList<>();
            for (final I input : inputList) {
                try {
                    futureList.add(taskProcessor.process(input));
                } catch (final CouldNotPerformException ex) {
                    if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                        exceptionStack = MultiException.push(AbstractExecutorService.class, ex, exceptionStack);
                    }
                }
            }
            MultiException.checkAndThrow(() -> "Could not execute all tasks!", exceptionStack);
            return futureList;
        } catch (CouldNotPerformException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Could not build future collection!", ex), LoggerFactory.getLogger(AbstractExecutorService.class));
        }
    }
}
