package org.dc.jul.processing;

/*
 * #%L
 * JUL Processing
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.iface.Processable;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
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
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });
        return future;
    }

    public static <I> ForkJoinTask<Void> toForkJoinTask(final Processable<I, Future<Void>> actionProcessor, final Collection<I> inputList) {
        return toForkJoinTask(actionProcessor, (Collection<Future<Void>> input) -> null, inputList);
    }

    public static <I, O, R> ForkJoinTask<R> toForkJoinTask(final Processable<I, Future<O>> actionProcessor, final Processable<Collection<Future<O>>, R> resultProcessor, final Collection<I> inputList) {
        return ForkJoinPool.commonPool().submit(new Callable<R>() {
            @Override
            public R call() throws Exception {
                MultiException.ExceptionStack exceptionStack = null;
                List<Future<O>> futureList = new ArrayList<>();
                for (I input : inputList) {
                    try {
                        futureList.add(actionProcessor.process(input));
                    } catch (CouldNotPerformException ex) {
                        exceptionStack = MultiException.push(this, ex, exceptionStack);
                    }
                }
                try {
                    for (Future<O> future : futureList) {
                        try {
                            future.get();
                        } catch (ExecutionException ex) {
                            exceptionStack = MultiException.push(this, ex, exceptionStack);
                        }
                    }
                } catch (InterruptedException ex) {
                    // cancel all pending actions.
                    futureList.stream().forEach((future) -> {
                        future.cancel(true);
                    });
                    throw ex;
                }
                MultiException.checkAndThrow("Could not apply all actions!", exceptionStack);
                return resultProcessor.process(futureList);
            }
        });
    }
}
