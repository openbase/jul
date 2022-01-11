package org.openbase.jul.exception;

/*-
 * #%L
 * JUL Exception
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

import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.exception.printer.Printer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.Arrays;
import java.util.Map;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class StackTracePrinter {

    private static final ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     * <p>
     * Note: The used log level will be ERROR.
     *
     * @param responsibleClass the class which is responsible for the printing.
     */
    public static void printStackTrace(final Class responsibleClass) {
        printStackTrace((String) null, LoggerFactory.getLogger(responsibleClass), LogLevel.ERROR);
    }

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     * <p>
     * Note: The used log level will be ERROR.
     *
     * @param logger the logger used for printing.
     */
    public static void printStackTrace(final Logger logger) {
        printStackTrace((String) null, logger, LogLevel.ERROR);
    }

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     * @param logger   the logger used for printing.
     * @param logLevel the log level used for logging the stack trace.
     */
    public static void printStackTrace(final Logger logger, final LogLevel logLevel) {
        printStackTrace((String) null, logger, logLevel);
    }

    /**
     * Method prints the given stack trace in a human readable way.
     *
     * @param stackTraces the stack trace to print.
     * @param logger      the logger used for printing.
     * @param logLevel    the log level used for logging the stack trace.
     */
    public static void printStackTrace(final StackTraceElement[] stackTraces, final Logger logger, final LogLevel logLevel) {
        printStackTrace(null, stackTraces, logger, logLevel);
    }

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     * @param message  the reason for printing the stack trace.
     * @param logger   the logger used for printing.
     * @param logLevel the log level used for logging the stack trace.
     */
    public static void printStackTrace(final String message, final Logger logger, final LogLevel logLevel) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        printStackTrace(message, Arrays.copyOfRange(stackTrace, 2, stackTrace.length), logger, logLevel);
    }

    /**
     * Method prints the given stack trace in a human readable way.
     *
     * @param message     the reason for printing the stack trace.
     * @param stackTraces the stack trace to print.
     * @param logger      the logger used for printing.
     * @param logLevel    the log level used for logging the stack trace.
     */
    public static void printStackTrace(final String message, final StackTraceElement[] stackTraces, final Logger logger, final LogLevel logLevel) {
        String stackTraceString = "";
        for (final StackTraceElement stackTrace : stackTraces) {
            stackTraceString += stackTrace.toString() + "\n";
        }

        Printer.print((message == null ? "" : message) + "\n=== Stacktrace ===\n" + stackTraceString + "==================", logLevel, logger);
    }

    /**
     * Marked as deprecated because of the erroneous name. Call printAllStackTraces instead.
     *
     * @param logger   the logger used for printing.
     * @param logLevel the level to print.
     */
    @Deprecated
    public static void printAllStackTrackes(final Logger logger, final LogLevel logLevel) {
        printAllStackTraces(null, logger, logLevel);
    }

    /**
     * Marked as deprecated because of the erroneous name. Call printAllStackTraces instead.
     *
     * @param filter   only thread where the name of the thread contains this given {@code filter} key are printed. If the filter is null, no filtering will be performed.
     * @param logger   the logger used for printing.
     * @param logLevel the level to print.
     */
    @Deprecated
    public static void printAllStackTrackes(final String filter, final Logger logger, final LogLevel logLevel) {
        printAllStackTraces(filter, logger, logLevel);
    }

    /**
     * Method prints the stack traces of all running java threads.
     * <p>
     * Note: The used log level will be ERROR.
     *
     * @param responsibleClass the class which is responsible for the printing.
     */
    public static void printAllStackTrace(final Class responsibleClass) {
        printStackTrace((String) null, LoggerFactory.getLogger(responsibleClass), LogLevel.ERROR);
    }

    /**
     * Method prints the stack traces of all running java threads via the given logger.
     *
     * @param logger   the logger used for printing.
     * @param logLevel the level to print.
     */
    public static void printAllStackTraces(final Logger logger, final LogLevel logLevel) {
        printAllStackTraces(null, logger, logLevel);
    }

    /**
     * Method prints the stack traces of all running java threads via the given logger.
     *
     * @param filter   only thread where the name of the thread contains this given {@code filter} key are printed. If the filter is null, no filtering will be performed.
     * @param logger   the logger used for printing.
     * @param logLevel the level to print.
     */
    public static void printAllStackTraces(final String filter, final Logger logger, final LogLevel logLevel) {
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            if (filter == null || entry.getKey().getName().contains(filter)) {
                StackTracePrinter.printStackTrace("Thread[" + entry.getKey().getName() + "] state[" + entry.getKey().getState().name() + "]", entry.getValue(), logger, logLevel);
            }
        }
    }

    /**
     * Method prints the stack traces of all running java threads via the given logger.
     *
     * @param filter                     only thread where the name of the thread contains this given {@code filter} key are printed. If the filter is null, no filtering will be performed.
     * @param logger                     the logger used for printing.
     * @param logLevel                   the level to print.
     * @param filterWaitingWorkerThreads filter worker threads that currently do not work on a task.
     */
    public static void printAllStackTraces(final String filter, final Logger logger, final LogLevel logLevel, final boolean filterWaitingWorkerThreads) {
        for (Map.Entry<Thread, StackTraceElement[]> entry : Thread.getAllStackTraces().entrySet()) {
            if (filter == null || entry.getKey().getName().contains(filter)) {
                if (filterWaitingWorkerThreads && entry.getValue().length > 4 && entry.getValue()[entry.getValue().length - 4].toString().contains("java.util.concurrent.ThreadPoolExecutor.getTask")) {
                    continue;
                }

                StackTracePrinter.printStackTrace("Thread[" + entry.getKey().getName() + "] state[" + entry.getKey().getState().name() + "]", entry.getValue(), logger, logLevel);
            }
        }
    }

    /**
     * Methods analyses the current stack traces of all threads an tries to detect deadlocks.
     * In case one is detected the affected stack traces are printed including the lock access.
     *
     * @param responsibleClass the responsible class used to generate the logger object used for the report in case deadlocks are detected.
     *
     * @return true in case deadlocks are detected, otherwise false.
     */
    public static boolean detectDeadLocksAndPrintStackTraces(final Class responsibleClass) {
        return detectDeadLocksAndPrintStackTraces(LoggerFactory.getLogger(responsibleClass));
    }

    /**
     * Methods analyses the current stack traces of all threads an tries to detect deadlocks.
     * In case one is detected the affected stack traces are printed including the lock access.
     *
     * @param logger the logger used for the report in case deadlocks are detected.
     *
     * @return true in case deadlocks are detected, otherwise false.
     */
    public static boolean detectDeadLocksAndPrintStackTraces(final Logger logger) {
        // before canceling pending actions lets just validate that the test did not cause any deadlocks
        final long[] deadlockedThreads = threadMXBean.findDeadlockedThreads();
        if (deadlockedThreads != null) {
            logger.error("Deadlock detected!");

            final Map<Thread, StackTraceElement[]> stackTraceMap = Thread.getAllStackTraces();
            for (final ThreadInfo threadInfo : threadMXBean.getThreadInfo(deadlockedThreads)) {

                // filter if thread was not a part of the deadlock any longer
                if (threadInfo == null) {
                    continue;
                }

                for (final Thread thread : stackTraceMap.keySet()) {

                    // filter non target thread
                    if (thread.getId() != threadInfo.getThreadId()) {
                        continue;
                    }

                    // print report
                    logger.error(threadInfo.toString().trim());
                    for (StackTraceElement stackTrance : thread.getStackTrace()) {
                        logger.error("\t" + stackTrance.toString().trim());
                    }
                }
            }
            return true;
        }
        return false;
    }
}
