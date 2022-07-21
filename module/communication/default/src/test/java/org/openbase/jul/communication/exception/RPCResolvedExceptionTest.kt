package org.openbase.jul.communication.exception

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.NotAvailableException
import org.openbase.type.communication.mqtt.ResponseType

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class RPCResolvedExceptionTest {

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