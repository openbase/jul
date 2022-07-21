package org.openbase.jul.communication.exception

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.ExceptionProcessor
import org.openbase.jul.exception.NotAvailableException
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.TestInstance
import org.openbase.type.communication.mqtt.ResponseType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
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

    @Test
    fun testExceptionResolving() {
        val thrownException = NotAvailableException("error")
        val wrappingException = CouldNotPerformException("Could not execute internal task", thrownException)
        val stackTrace = wrappingException.stackTraceToString()
        val response = ResponseType.Response.newBuilder().setError(stackTrace).build()

        val ex = RPCException(response.error, null)
        val resolveRPCException = RPCResolvedException.resolveRPCException(ex)

        resolveRPCException::class.java shouldBe CouldNotPerformException::class.java
        resolveRPCException shouldBe wrappingException
        resolveRPCException.cause shouldNotBe null

        resolveRPCException.cause?.let { cause ->
            cause::class.java shouldBe NotAvailableException::class.java
            cause.message shouldBe thrownException.message
        }
    }
}
