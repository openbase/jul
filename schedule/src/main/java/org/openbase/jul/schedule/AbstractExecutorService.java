package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.pattern.provider.StableProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.*;
import java.util.concurrent.*;

/**
 * @param <ES> The internal execution service.
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractExecutorService<ES extends java.util.concurrent.AbstractExecutorService> implements Shutdownable {

    /**
     * Default shutdown delay in milliseconds.
     */
    public static final long DEFAULT_SHUTDOWN_DELAY = 5000;

    /**
     * Default shutdown time in milliseconds.
     */
    public static final long DEFAULT_SHUTDOWN_TIME = 30000;

    /**
     * Default shutdown time in milliseconds.
     */
    public static final long SMART_SHUTDOWN_TIMEOUT = 30000;

    /**
     * The rate for printing feedback if the shutdown is delayed.
     */
    public static final long SMART_SHUTDOWN_STATUS_PRINT_RATE = 1000;

    /**
     * Report rate for the debug mode in milliseconds.
     */
    public static final long DEFAULT_REPORT_RATE = 60000;

    /**
     * The ratio of the threads which can be used until pool overload warnings are periodically printed.
     */
    public static final double DEFAULT_WARNING_RATIO = .9d;

    /**
     * The internally used executor service.
     */
    protected final ES executorService;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * Register gloabl UncaughtExceptionHandler
     */
    static {
        Thread.setDefaultUncaughtExceptionHandler(new UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread t, Throwable ex) {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException("UncaughtException found!", t, ex), LoggerFactory.getLogger(getClass()));
            }
        });
    }

    final StableProvider<Integer> currentTaskCountProvider;
    final StableProvider<Integer> currentThreadCountProvider;
    final StableProvider<Integer> maxTaskCountProvider;
    boolean shutdownInitiated = false;

    public AbstractExecutorService(final ES executorService, StableProvider<Integer> currentTaskCountProvider, StableProvider<Integer> maxTaskCountProvider, StableProvider<Integer> currentThreadCountProvider) throws CouldNotPerformException {
        this.executorService = executorService;
        this.currentTaskCountProvider = currentTaskCountProvider;
        this.maxTaskCountProvider = maxTaskCountProvider;
        this.currentThreadCountProvider = currentThreadCountProvider;
        this.initReportService();
        Shutdownable.registerShutdownHook(this, DEFAULT_SHUTDOWN_DELAY);
    }

    private Runnable initReportService() {
        final Runnable reportService = () -> {
            final boolean overload;
            if (currentTaskCountProvider.get() >= maxTaskCountProvider.get()) {
                overload = true;
                logger.warn("Further tasks will be rejected because executor service overload is detected!");
                if (JPService.verboseMode()) {
                    StackTracePrinter.printAllStackTraces("pool", logger, LogLevel.INFO);
                }
            } else if (currentTaskCountProvider.get() >= ((double) maxTaskCountProvider.get() * DEFAULT_WARNING_RATIO)) {
                overload = true;
                logger.warn("High Executor service load detected! This can cause system instability issues!");
                if (JPService.verboseMode()) {
                    StackTracePrinter.printAllStackTraces("pool", logger, LogLevel.INFO);
                }
            } else {
                overload = false;
            }

            if (JPService.debugMode() || overload) {
                logger.info("Executor load " + getExecutorLoad() + "% (" + currentTaskCountProvider.get()  + " tasks are processed by " + currentThreadCountProvider.get() + " threads).");
            }
        };
        final ScheduledExecutorService scheduledExecutorService;
        scheduledExecutorService = executorService instanceof ScheduledExecutorService ? ((ScheduledExecutorService) executorService) : GlobalScheduledExecutorService.getInstance().getExecutorService();
        scheduledExecutorService.scheduleAtFixedRate(reportService, DEFAULT_REPORT_RATE, DEFAULT_REPORT_RATE, TimeUnit.MILLISECONDS);
        return reportService;
    }

    public int getExecutorLoad() {
        if (maxTaskCountProvider.get() == 0) {
            if (currentTaskCountProvider.get() == 0) {
                return 0;
            } else {
                return 100;
            }
        }
        return ((int) (((double) currentTaskCountProvider.get() / (double) maxTaskCountProvider.get()) * 100));
    }

    public <T> Future<T> internalSubmit(Callable<T> task) {
        if(shutdownInitiated) {
            throw new RejectedExecutionException(new ShutdownInProgressException("ExecutorService"));
        }
        return executorService.submit(task);
    }

    public Future<?> internalSubmit(Runnable task) {
        if(shutdownInitiated) {
            throw new RejectedExecutionException(new ShutdownInProgressException("ExecutorService"));
        }
        return executorService.submit(task);
    }

    public void internalExecute(final Runnable runnable) {
        if(shutdownInitiated) {
            throw new RejectedExecutionException(new ShutdownInProgressException("ExecutorService"));
        }
        executorService.execute(runnable);
    }

    public ES getExecutorService() {
        return executorService;
    }

    @Override
    public void shutdown() {
        shutdownInitiated = true;
        smartShutdown();
    }

    public void smartShutdown() {
        long timeout = SMART_SHUTDOWN_TIMEOUT;
        int lastTaskCount = Integer.MAX_VALUE;
        while (currentTaskCountProvider.get() != 0) {

            if (currentTaskCountProvider.get() >= lastTaskCount) {
                timeout -= SMART_SHUTDOWN_STATUS_PRINT_RATE;
            } else {
                logger.info("Waiting for " + currentTaskCountProvider.get() + " tasks to continue the shutdown.");
            }

            if (timeout <= 0) {
                logger.warn("Smart shutdown timeout reached!");

                if(JPService.testMode() || JPService.verboseMode()) {
                    StackTracePrinter.printAllStackTraces("pool", logger, LogLevel.INFO, true);
                }
                break;
            }

            lastTaskCount = currentTaskCountProvider.get();
            try {
                Thread.sleep(SMART_SHUTDOWN_STATUS_PRINT_RATE);
            } catch (InterruptedException ex) {
                logger.warn("Smart shutdown skipped!");
                break;
            }
        }
        shutdown(DEFAULT_SHUTDOWN_TIME, TimeUnit.MILLISECONDS);
    }

    public void shutdown(final long shutdownTimeout, final TimeUnit timeUnit) {
        logger.debug("Shutdown global executor service...");

        final int activeCount = currentTaskCountProvider.get();
        if (activeCount != 0) {
            logger.info("Global executor shutdown forced: " + activeCount + " tasks will be skipped...");
        }

        List<Runnable> droppedTasks = executorService.shutdownNow();
        if (!droppedTasks.isEmpty()) {
            logger.debug(droppedTasks.size() + " tasks dropped!");
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
     * Simply prints the class name.
     *
     * @return the class name of this instance.
     */
    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
