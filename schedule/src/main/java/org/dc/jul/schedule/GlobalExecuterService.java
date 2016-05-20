/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.schedule;

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
public class GlobalExecuterService implements Shutdownable {

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(GlobalExecuterService.class);

    private static GlobalExecuterService instance;

    private final ExecutorService executionService;

    private GlobalExecuterService() {
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

    public static synchronized GlobalExecuterService getInstance() {
        if (instance == null) {
            instance = new GlobalExecuterService();
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
