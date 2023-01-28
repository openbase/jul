package org.openbase.jul.exception

import org.openbase.jul.exception.printer.LogLevel
import org.openbase.jul.exception.printer.Printer
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.management.ManagementFactory
import java.util.*

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
 */ /**
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
object StackTracePrinter {
    private val threadMXBean = ManagementFactory.getThreadMXBean()

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     *
     * Note: The used log level will be ERROR.
     *
     * @param responsibleClass the class which is responsible for the printing.
     */
    fun printStackTrace(responsibleClass: Class<*>?) {
        printStackTrace(null as String?, LoggerFactory.getLogger(responsibleClass), LogLevel.ERROR)
    }

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     *
     * Note: The used log level will be ERROR.
     *
     * @param logger the logger used for printing.
     */
    @JvmStatic
    fun printStackTrace(logger: Logger?) {
        printStackTrace(null as String?, logger, LogLevel.ERROR)
    }

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     * @param logger   the logger used for printing.
     * @param logLevel the log level used for logging the stack trace.
     */
    @JvmStatic
    fun printStackTrace(logger: Logger?, logLevel: LogLevel?) {
        printStackTrace(null as String?, logger, logLevel)
    }

    /**
     * Method prints the given stack trace in a human readable way.
     *
     * @param stackTraces the stack trace to print.
     * @param logger      the logger used for printing.
     * @param logLevel    the log level used for logging the stack trace.
     */
    fun printStackTrace(stackTraces: Array<StackTraceElement>?, logger: Logger?, logLevel: LogLevel?) {
        printStackTrace(null, stackTraces, logger, logLevel)
    }

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     * @param message  the reason for printing the stack trace.
     * @param logger   the logger used for printing.
     * @param logLevel the log level used for logging the stack trace.
     */
    @JvmStatic
    fun printStackTrace(message: String?, logger: Logger?, logLevel: LogLevel?) {
        val stackTrace = Thread.currentThread().stackTrace
        printStackTrace(message, Arrays.copyOfRange(stackTrace, 2, stackTrace.size), logger, logLevel)
    }

    /**
     * Method prints the given stack trace in a human readable way.
     *
     * @param message     the reason for printing the stack trace.
     * @param stackTraces the stack trace to print.
     * @param logger      the logger used for printing.
     * @param logLevel    the log level used for logging the stack trace.
     */
    @JvmStatic
    fun printStackTrace(
        message: String?,
        stackTraces: Array<StackTraceElement>?,
        logger: Logger?,
        logLevel: LogLevel?,
    ) {
        var stackTraceString = ""
        for (stackTrace in stackTraces!!) {
            stackTraceString += """
                $stackTrace
                
                """.trimIndent()
        }
        Printer.print(
            """
    ${message ?: ""}
    === Stacktrace ===
    $stackTraceString==================
    """.trimIndent(), logLevel, logger
        )
    }

    /**
     * Method prints the stack traces of all running java threads.
     *
     *
     * Note: The used log level will be ERROR.
     *
     * @param responsibleClass the class which is responsible for the printing.
     */
    @JvmStatic
    fun printAllStackTraces(responsibleClass: Class<*>?) {
        printStackTrace(null as String?, null, LoggerFactory.getLogger(responsibleClass), LogLevel.ERROR)
    }

    /**
     * Method prints the stack traces of all running java threads via the given logger.
     *
     * @param logger   the logger used for printing.
     * @param logLevel the level to print.
     */
    @JvmStatic
    fun printAllStackTraces(logger: Logger?, logLevel: LogLevel?) {
        printAllStackTraces(null, null, logger, logLevel)
    }

    /**
     * Method prints the stack traces of all running java threads via the given logger.
     *
     * @param threadFilter   only threads where the name of the thread contains this given `threadFilter` key are printed. If the filter is null, no filtering will be performed.
     * @param classFilter    only stacktraces that contain any relation to the given class are printed.
     * @param logger   the logger used for printing.
     * @param logLevel the level to print.
     */
    @JvmStatic
    fun printAllStackTraces(threadFilter: String?, classFilter: Class<*>?, logger: Logger?, logLevel: LogLevel?) {
        for ((key, value) in Thread.getAllStackTraces()) {

            // apply thread filter
            if (threadFilter != null && !key.name.contains(threadFilter)) {
                continue
            }

            // apply class filter
            if (classFilter != null && Arrays.stream(value)
                    .noneMatch { it: StackTraceElement -> it.className.contains(classFilter.simpleName) }
            ) {
                continue
            }
            printStackTrace("Thread[" + key.name + "] state[" + key.state.name + "]", value, logger, logLevel)
        }
    }

    /**
     * Method prints the stack traces of all running java threads via the given logger.
     *
     * @param filter                     only thread where the name of the thread contains this given `filter` key are printed. If the filter is null, no filtering will be performed.
     * @param logger                     the logger used for printing.
     * @param logLevel                   the level to print.
     * @param filterWaitingWorkerThreads filter worker threads that currently do not work on a task.
     */
    @JvmStatic
    fun printAllStackTraces(
        filter: String?,
        logger: Logger?,
        logLevel: LogLevel?,
        filterWaitingWorkerThreads: Boolean = false,
    ) {
        Thread.getAllStackTraces()
            .filter { (thread, stacktrace) ->
                filter == null
                        || thread.name.contains(filter)
                        || stacktrace.any { it.className.contains("org.openbase") }
            }
            .filterNot { (_, stacktrace) ->
                filterWaitingWorkerThreads && stacktrace.size > 4 && stacktrace[stacktrace.size - 4].toString()
                    .contains("java.util.concurrent.ThreadPoolExecutor.getTask")
            }
            .forEach { (thread, stacktrace) ->
                printStackTrace(
                    message = "Thread[" + thread.name + "] state[" + thread.state.name + "]",
                    stackTraces = stacktrace,
                    logger = logger,
                    logLevel = logLevel
                )
            }
    }

    /**
     * Methods analyses the current stack traces of all threads and tries to detect deadlocks.
     * In case one is detected the affected stack traces are printed including the lock access.
     *
     * @param responsibleClass the responsible class used to generate the logger object used for the report in case deadlocks are detected.
     *
     * @return true in case deadlocks are detected, otherwise false.
     */
    @JvmStatic
    fun detectDeadLocksAndPrintStackTraces(responsibleClass: Class<*>?): Boolean {
        return detectDeadLocksAndPrintStackTraces(LoggerFactory.getLogger(responsibleClass))
    }

    /**
     * Methods analyses the current stack traces of all threads and tries to detect deadlocks.
     * In case one is detected the affected stack traces are printed including the lock access.
     *
     * @param logger the logger used for the report in case deadlocks are detected.
     *
     * @return true in case deadlocks are detected, otherwise false.
     */
    @JvmStatic
    fun detectDeadLocksAndPrintStackTraces(logger: Logger): Boolean {
        // before canceling pending actions lets just validate that the test did not cause any deadlocks
        val deadlockedThreads = threadMXBean.findDeadlockedThreads()
        if (deadlockedThreads != null) {
            logger.error("Deadlock detected!")
            val stackTraceMap = Thread.getAllStackTraces()
            for (threadInfo in threadMXBean.getThreadInfo(deadlockedThreads)) {

                // filter if thread was not a part of the deadlock any longer
                if (threadInfo == null) {
                    continue
                }
                for (thread in stackTraceMap.keys) {

                    // filter non target thread
                    if (thread.id != threadInfo.threadId) {
                        continue
                    }

                    // print report
                    logger.error(threadInfo.toString().trim { it <= ' ' })
                    for (stackTrance in thread.stackTrace) {
                        logger.error("\t" + stackTrance.toString().trim { it <= ' ' })
                    }
                }
            }
            return true
        }
        return false
    }
}
