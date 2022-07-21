package org.openbase.jul.communication.exception

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.ExceptionProcessor
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.exception.printer.ExceptionPrinter

internal class RPCResolvedExceptionTest {

    @Test
    fun testExceptionResolution() {

        val missingEntity = "mize"

        val testException = CouldNotPerformException(
            message = "root cause",
            cause = CouldNotPerformException(
                message = "second level cause",
                cause = NotAvailableException(missingEntity)
            )
        )

        val rpcException = RPCException(message = testException.stackTraceToString())

        RPCResolvedException
            .resolveRPCException(rpcException)
            .let { result ->
                ExceptionProcessor.getInitialCause(result).let { initialCause ->
                    Assertions.assertEquals(initialCause.message, NotAvailableException(missingEntity).message)
                }
            }

        RPCResolvedException(rpcException)
            .let { result ->
                ExceptionProcessor.getInitialCause(result).let { initialCause ->
                    Assertions.assertEquals(initialCause.message, NotAvailableException(missingEntity).message)

                    println(result.stackTraceToString())
                    ExceptionPrinter.printHistory(result, System.out)
                }
            }
    }
}
