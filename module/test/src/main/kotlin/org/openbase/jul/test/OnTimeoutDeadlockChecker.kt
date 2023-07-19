package org.openbase.jul.communication.mqtt.test

import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler
import org.openbase.jul.exception.StackTracePrinter
import org.openbase.jul.exception.printer.LogLevel
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeoutException

open class OnTimeoutDeadlockChecker(
    private val filter: String? = null,
) : TestExecutionExceptionHandler {

    private val logger = LoggerFactory.getLogger(javaClass)

    override fun handleTestExecutionException(context: ExtensionContext?, throwable: Throwable?) {
        when (throwable) {
            is TimeoutException -> {

                logger.warn("Timeout caught during test, analyse stacktrace...")
                // detect deadlocks
                if (StackTracePrinter.detectDeadLocksAndPrintStackTraces(logger)) {
                    return
                }
                logger.info(
                    "No deadlocks were found, so stacktraces of all threads are printed for further investigation..."
                )
                StackTracePrinter.printAllStackTraces(
                    filter,
                    logger,
                    LogLevel.WARN,
                    false
                )
            }
        }

        throwable?.also { throw it }
    }
}
