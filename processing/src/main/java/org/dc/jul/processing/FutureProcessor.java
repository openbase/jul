package org.dc.jul.processing;

import java.util.concurrent.Callable;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class FutureProcessor {

    /**
     * Method transforms a callable into a Future object.
     *
     * @param <T>
     * @param future the future object to wrap.
     * @return the Future representing the call state.
     */
    public static <T> Future<T> toFuture(final Future<T> internalFuture) {
        ForkJoinTask<Object> submit = ForkJoinPool.commonPool().submit(null);
        submit.isCompletedNormally()
        Future<T> future = new Future<>();
        Future.runAsync(() -> {
            try {
                future.complete(internalFuture.get());
            } catch (InterruptedException e) {
                future.completeExceptionally(e);
                Thread.currentThread().interrupt();
            } catch (Exception e) {
                future.completeExceptionally(e);
            }

        });
        return future;
//        Future<T> future = new Future<>();
//        future.
//        return Future.supplyAsync(() -> {
//            try {
//                return future.get();
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//                throw new CouldNotProcessException(ex);
//            } catch (ExecutionException ex) {
//                throw new CouldNotProcessException(ex);
//            }
//        });
    }

     public static <T> Future<T> toFuture(final Callable<T> callable) {
        Future<T> future = new Future<>();
        Future.runAsync(() -> {
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

//    /**
//     * Method transforms a callable into a Future object.
//     *
//     * @param <T>
//     * @param callable the callable to wrap.
//     * @return the Future representing the call state.
//     */
//    public static <T> Future<T> toFuture(final Callable<T> callable) {
//        return Future.supplyAsync(() -> {
//            try {
//                return callable.call();
//            } catch (InterruptedException ex) {
//                Thread.currentThread().interrupt();
//                throw new CouldNotProcessException(ex);
//            } catch (Exception ex) {
//                throw new CouldNotProcessException(ex);
//            }
//        });
//    }
}
