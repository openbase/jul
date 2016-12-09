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
import java.util.Collection;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.iface.Processable;

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
            
    
    private static GlobalCachedExecutorService instance;

    private GlobalCachedExecutorService() {
        super((ThreadPoolExecutor) Executors.newCachedThreadPool());
        
        // configure executor service
        executorService.setKeepAliveTime(DEFAULT_KEEP_ALIVE_TIME, TimeUnit.MILLISECONDS);
        executorService.setMaximumPoolSize(DEFAULT_MAX_POOL_SIZE);
        executorService.setCorePoolSize(DEFAULT_CORE_POOL_SIZE);
        // ==========================
    }

    public static synchronized GlobalCachedExecutorService getInstance() {
        if (instance == null) {
            instance = new GlobalCachedExecutorService();
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

    /**
     * This method applies an error handler to the given future object.
     * In case the given timeout is expired or the future processing fails the error processor is processed with the occured exception as argument.
     * The receive a future should be submitted to any execution service or handled externally.
     *
     * @param future the future on which is the error processor is registered.
     * @param timeout the timeout.
     * @param errorProcessor the processable which handles thrown exceptions
     * @param timeUnit the unit of the timeout.
     * @return the future of the error handler.
     * @throws CouldNotPerformException thrown by the errorProcessor
     */
    public static Future applyErrorHandling(final Future future, final Processable<Exception, Void> errorProcessor, final long timeout, final TimeUnit timeUnit) throws CouldNotPerformException {
        return AbstractExecutorService.applyErrorHandling(future, errorProcessor, timeout, timeUnit, getInstance().getExecutorService());
    }

    public static <I> Future<Void> allOf(final Processable<I, Future<Void>> actionProcessor, final Collection<I> inputList) {
        return allOf(actionProcessor, (Collection<Future<Void>> input) -> null, inputList, getInstance().getExecutorService());
    }

    public static <I, O, R> Future<R> allOf(final Processable<I, Future<O>> actionProcessor, final Processable<Collection<Future<O>>, R> resultProcessor, final Collection<I> inputList) {
        return allOf(actionProcessor, resultProcessor, inputList, getInstance().getExecutorService());
    }

    public static <T> Future<T> allOf(final Collection<Future> futureCollection, T returnValue) {
        return allOf(futureCollection, returnValue, getInstance().getExecutorService());
    }
}
