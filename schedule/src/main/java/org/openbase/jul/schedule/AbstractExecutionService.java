package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.JPDebugMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.iface.Processable;
import org.openbase.jul.iface.Shutdownable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <ES> The internal execution service.
 */
public abstract class AbstractExecutionService<ES extends ThreadPoolExecutor> implements Shutdownable {

    public static final long DEFAULT_SHUTDOWN_TIME = 5;

    /**
     * Report rate for the debug mode in milliseconds.
     */
    public static final long DEFAULT_REPORT_RATE = 60000;

    public static final double DEFAULT_WARNING_RATIO = .9d;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected final ES executorService;

    public AbstractExecutionService(final ES executorService) {
        this.executorService = executorService;
        this.initReportService();
        Shutdownable.registerShutdownHook(this);
    }

    private Runnable initReportService() {
        final Runnable reportService = new Runnable() {
            @Override
            public void run() {
                try {
                    final boolean overload;
                    if (executorService.getActiveCount() == executorService.getPoolSize()) {
                        overload = true;
                        logger.warn("Further tasks will be rejected because executor service overload detected!");
                    } else if (executorService.getActiveCount() >= ((double) executorService.getPoolSize() * DEFAULT_WARNING_RATIO)) {
                        overload = true;
                        logger.warn("High Executor service load of detected! This can cause system instability issues!");
                    } else {
                        overload = false;
                    }

                    if (overload || JPService.getProperty(JPDebugMode.class).getValue()) {
                        logger.info("ExecutorLoad " + getExecutorLoad() + "% [" + executorService.getActiveCount() + " active threads of " + executorService.getPoolSize() + " processing " + executorService.getTaskCount() + " tasks]");
                    }
                } catch (JPNotAvailableException ex) {
                    logger.warn("Could not detect debug mode!", ex);
                }
            }
        };
        final ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = executorService instanceof ScheduledExecutorService ? ((ScheduledExecutorService) executorService) : GlobalScheduledExecutionService.getInstance().getExecutorService();
        scheduledExecutorService.scheduleAtFixedRate(reportService, DEFAULT_REPORT_RATE, DEFAULT_REPORT_RATE, TimeUnit.MILLISECONDS);
        return reportService;
    }

    public int getExecutorLoad() {
        if (executorService.getPoolSize() == 0) {
            if (executorService.getActiveCount() == 0) {
                return 0;
            } else {
                return 100;
            }
        }
        return ((int) (((double) executorService.getActiveCount() / (double) executorService.getPoolSize()) * 100));
    }

    public <T> Future<T> internalSubmit(Callable<T> task) {
        return executorService.submit(task);
    }

    public Future<?> internalSubmit(Runnable task) {
        return executorService.submit(task);
    }

    public void internalExecute(final Runnable runnable) {
        executorService.execute(runnable);
    }

    public ES getExecutorService() {
        return executorService;
    }

    @Override
    public void shutdown() {
        shutdown(DEFAULT_SHUTDOWN_TIME, TimeUnit.SECONDS);
    }

    public void shutdown(final long shutdownTimeout, final TimeUnit timeUnit) {
        logger.info("Shutdown global executor service...");
        List<Runnable> droppedTasks = executorService.shutdownNow();
        if (!droppedTasks.isEmpty()) {
            logger.info("Global executor shutdown forced: " + droppedTasks.size() + " tasks will be skipped...");
        }
        try {
            if (!executorService.awaitTermination(shutdownTimeout, timeUnit)) {
                logger.error("Executor did not terminate before shutdown Timeout[" + shutdownTimeout + " " + timeUnit.name().toLowerCase() + "] expired!");
                forceShutdown();
            }
        } catch (InterruptedException ex) {
            forceShutdown();
            Thread.currentThread().interrupt();
        }
    }

    public void forceShutdown() {
        for (int i = 0; i < 10; ++i) {
            executorService.shutdownNow();
            try {
                Thread.sleep(100);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /**
     * This method applies an error handler to the given future object.
     * In case the given timeout is expired or the future processing fails the error processor is processed with the occured exception as argument.
     * The receive a future should be submitted to any execution service or handled externally.
     *
     * @param future the future on which is the error processor is registered.
     * @param timeout the timeout.
     * @param errorProcessor the processable which handles thrown exceptions
     * @param timeUnit the unit of the timeout.
     * @param executorService the execution service to apply the handler.
     * @return the future of the error handler.
     * @throws CouldNotPerformException thrown by the errorProcessor
     */
    public static Future applyErrorHandling(final Future future, final Processable<Exception, Void> errorProcessor, final long timeout, final TimeUnit timeUnit, final ExecutorService executorService) throws CouldNotPerformException {
        return executorService.submit(() -> {
            try {
                future.get(timeout, timeUnit);
            } catch (ExecutionException | InterruptedException | TimeoutException ex) {
                errorProcessor.process(ex);
            }
            return null;
        });
    }

    public static <I> Future<Void> allOf(final Processable<I, Future<Void>> actionProcessor, final Collection<I> inputList, final ExecutorService executorService) {
        return allOf(actionProcessor, (Collection<Future<Void>> input) -> null, inputList, executorService);
    }

    public static <I, O, R> Future<R> allOf(final Processable<I, Future<O>> actionProcessor, final Processable<Collection<Future<O>>, R> resultProcessor, final Collection<I> inputList, final ExecutorService executorService) {
        return executorService.submit(new Callable<R>() {
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

    public static <T> Future<T> allOf(final Collection<Future> futureCollection, T returnValue, final ExecutorService executorService) {
        return executorService.submit(new Callable<T>() {
            @Override
            public T call() throws Exception {
                MultiException.ExceptionStack exceptionStack = null;
                try {
                    for (Future future : futureCollection) {
                        try {
                            future.get();
                        } catch (ExecutionException ex) {
                            exceptionStack = MultiException.push(this, ex, exceptionStack);
                        }
                    }
                } catch (InterruptedException ex) {
                    // cancel all pending actions.
                    futureCollection.stream().forEach((future) -> {
                        future.cancel(true);
                    });
                    throw ex;
                }
                MultiException.checkAndThrow("Could not apply all actions!", exceptionStack);
                return returnValue;
            }
        });
    }
}
