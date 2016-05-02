package org.dc.jul.processing;

import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.ExecutionException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class FutureTests {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws InterruptedException {
        try {
            System.out.println("start");

//            Future<String> future = Future.supplyAsync(new Supplier<String>() {
//
//                @Override
//                public String get() {
//
//                    System.out.println("start get");
//                    try {
//                        while (!Thread.interrupted()) {
//                            Thread.sleep(100);
//                            System.out.println("processing...");
//                        }
//                        System.out.println("stop get");
//                        return "finished";
//                    } catch (InterruptedException ex) {
//                        System.out.println("ups");
//                        throw new RuntimeException(ex);
////                        return "fake";
//                    }
//                }
//            });

            Future<String> future = FutureProcessor.toFuture(new Callable<String>() {

                @Override
                public String call() throws Exception {
                    while (true) {
                        if(Thread.interrupted()) {
                            System.out.println("cancel processing...!");
                            Thread.sleep(100);
                            throw new InterruptedException();
                        }
                        Thread.sleep(100);
                        System.out.println("processing...");
                    }
//                    System.out.println("stop get");
//                    return "finished";
                }
            });

            Thread.sleep(200);
            try {
                System.out.println("cancel: " + future.cancel(true));
            } catch (RuntimeException ex) {
                System.out.println("error during cancelation!");
                ExceptionPrinter.printHistory(ex, LoggerFactory.getLogger(FutureTests.class));
            }

            try {
                System.out.println("result: " + future.get());
            } catch (RuntimeException | ExecutionException ex) {
                System.out.println("execution failed!");
                ex.printStackTrace(System.err);
            }

            future.exceptionally((Throwable t) -> {
                ExceptionPrinter.printHistory(t, LoggerFactory.getLogger(FutureTests.class));
                return "Was willst du!!!";
            });

            try {
                while (!future.isDone()) {
                    System.out.println("wait...");
                    Thread.sleep(1000);
                }
            } catch (Exception ex) {
                System.out.println("error during wait!");
                ExceptionPrinter.printHistory(ex, LoggerFactory.getLogger(FutureTests.class));
            }
            System.out.println("end...");
            Thread.sleep(1000);
        } catch (Exception ex) {
            System.out.println("main crashed!");
            ExceptionPrinter.printHistory(ex, LoggerFactory.getLogger(FutureTests.class));
            ex.printStackTrace();
            Thread.sleep(10000);
        }

        while (true) {
            Thread.sleep(100);
            System.out.println("further action...");
        }

    }
}
