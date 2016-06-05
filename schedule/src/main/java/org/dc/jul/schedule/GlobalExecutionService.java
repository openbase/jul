/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.schedule;

/*
 * #%L
 * JUL Schedule
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
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.iface.Shutdownable;
import org.slf4j.LoggerFactory;

/**
 *
 * @author divine
 */
public class GlobalExecutionService implements Shutdownable {

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(GlobalExecutionService.class);

    private static GlobalExecutionService instance;

    private final ExecutorService executionService;

    private GlobalExecutionService() {
        this.executionService = Executors.newCachedThreadPool();

        Runtime.getRuntime().addShutdownHook(new Thread() {

            @Override
            public void run() {
                if (instance != null) {
                    try {
                        instance.shutdown();
                    } catch (InterruptedException ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("ExecutionService shutdown was interruped!", ex), logger);
                        Thread.currentThread().interrupt();
                    }
                }
            }
        });
    }

    public static synchronized GlobalExecutionService getInstance() {
        if (instance == null) {
            instance = new GlobalExecutionService();
        }
        return instance;
    }

    public static <T> Future<T> submit(Callable<T> task) {
        return getInstance().executionService.submit(task);
    }

    public static Future<?> submit(Runnable task) {
        return getInstance().executionService.submit(task);
    }

    @Override
    public void shutdown() throws InterruptedException {
        executionService.shutdownNow();
    }

}
