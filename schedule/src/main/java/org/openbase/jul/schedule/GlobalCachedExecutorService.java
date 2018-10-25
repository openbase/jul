package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import java.util.concurrent.*;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class GlobalCachedExecutorService extends AbstractExecutorService<ThreadPoolExecutor> {

    /**
     * Keep alive time in milli seconds.
     */
    public static final long DEFAULT_KEEP_ALIVE_TIME = 60000;

    /**
     * The default maximal pool size. If this thread amount is reached further tasks will be rejected.
     */
    public static final int DEFAULT_MAX_POOL_SIZE = 2000;

    /**
     * The core thread pool size.
     */
    public static final int DEFAULT_CORE_POOL_SIZE = 100;

//    private static GlobalCachedExecutorService instance;
//
//    private static ForkJoinPool executor = new ForkJoinPool
//            (200,
//                    ForkJoinPool.defaultForkJoinWorkerThreadFactory,
//                    null, true);
//
//    GlobalCachedExecutorService() throws CouldNotPerformException {
//        super(executor, () -> executor.getQueuedSubmissionCount(), () -> 1000, () -> executor.getActiveThreadCount());

    private static GlobalCachedExecutorService instance;

    private static ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newCachedThreadPool();

    GlobalCachedExecutorService() throws CouldNotPerformException {
        super(executor, () -> executor.getActiveCount(), () -> executor.getMaximumPoolSize(), () -> executor.getPoolSize());

//    GlobalCachedExecutorService() throws CouldNotPerformException {
//        super(new ThreadPoolExecutor(0, DEFAULT_MAX_POOL_SIZE,
//                DEFAULT_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS,
//                ForkJoinPool.defaultForkJoinWorkerThreadFactory));
        // configure executor service
        executorService.setKeepAliveTime(DEFAULT_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS);
        executorService.setMaximumPoolSize(DEFAULT_MAX_POOL_SIZE);
        executorService.setCorePoolSize(DEFAULT_CORE_POOL_SIZE);
        // ==========================
    }

    public static synchronized GlobalCachedExecutorService getInstance() {
        if (instance == null) {
            try {
                instance = new GlobalCachedExecutorService();
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not create executor service!", ex, LoggerFactory.getLogger(GlobalCachedExecutorService.class));
            }
        }
        return instance;
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return getInstance().internalSubmit(task);
    }

    public static Future<?> submit(Runnable task) {
        return getInstance().internalSubmit(task);
    }

    public static void execute(final Runnable runnable) {
        getInstance().internalExecute(runnable);
    }
}
