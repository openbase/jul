package org.openbase.jul.communication.exception

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.ExceptionProcessor
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.exception.printer.ExceptionPrinter

internal class RPCResolvedExceptionTest {

    companion object {
        const val missingEntity = "mize"
        const val rootMessage = "root cause"
        const val secondLevelMessage = "second level cause"
    }

    @Test
    fun testExceptionResolution() {

        val testException = CouldNotPerformException(
            message = rootMessage,
            cause = CouldNotPerformException(
                message = secondLevelMessage,
                cause = NotAvailableException(missingEntity)
            )
        )

        RPCException(message = testException.stackTraceToString()).let { rpcException ->

            // resolve exception via explicit method call
            RPCResolvedException
                .resolveRPCException(rpcException)
                .let { result ->
                    ExceptionProcessor.getInitialCause(result).let { initialCause ->
                        Assertions.assertEquals(initialCause.message, NotAvailableException(missingEntity).message)
                    }
                }

            // resolve exception via constructor call
            RPCResolvedException(rpcException)
                .let { result ->
                    ExceptionProcessor.getInitialCause(result).let { initialCause ->
                        Assertions.assertEquals(initialCause.message, NotAvailableException(missingEntity).message)
                    }
                }
        }
    }
}
