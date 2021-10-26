package org.openbase.jul.communication.mqtt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.mqtt.PrimitiveType
import org.openbase.type.communication.mqtt.RequestType
import org.openbase.type.communication.mqtt.ResponseType
import java.lang.reflect.Method
import kotlin.Any
import com.google.protobuf.Any as protoAny

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RPCMethodTest {

    @Test
    fun `test anyToProtoAny and protoAnyToAny Conversion`() {
        fun <T : Any> backAndForth(value: T): T {
            val anyToProtoAnyConverter = RPCMethod.anyToProtoAny(value::class)
            val protoAnyToAnyConverter = RPCMethod.protoAnyToAny(value::class)
            return protoAnyToAnyConverter(anyToProtoAnyConverter(value)) as T
        }

        val valueInt = 42
        backAndForth(valueInt) shouldBe valueInt

        val valueFloat = 3.141f
        backAndForth(valueFloat) shouldBe valueFloat

        val valueDouble = 2.781
        backAndForth(valueDouble) shouldBe valueDouble

        val valueBoolean = true
        backAndForth(valueBoolean) shouldBe valueBoolean

        val valueString = "Hello"
        backAndForth(valueString) shouldBe valueString

        val valueMessage =
            ResponseType.Response.newBuilder().setStatus(ResponseType.Response.Status.ACKNOWLEDGED).build()
        backAndForth(valueMessage) shouldBe valueMessage

        shouldThrow<CouldNotPerformException> { RPCMethod.anyToProtoAny(Any::class) }
        shouldThrow<CouldNotPerformException> { RPCMethod.protoAnyToAny(Any::class) }
    }

    internal class MethodProvider {

        fun idle() {
            println("Idle called")
        }

        fun ping(): Long {
            println("Ping")
            return 0
        }

        fun request(request: RequestType.Request): ResponseType.Response {
            println("Request")
            return ResponseType.Response.newBuilder().setStatus(ResponseType.Response.Status.FINISHED).build()
        }

        fun setValue(value: String) {
            println("Set value to $value")
        }
    }

    @Test
    fun `test invoke rpc method`() {
        /*val args = arrayOf(5, 6);
        val argsAsProtoAny = args
            .map { arg -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setInt(arg).build()) }
        val argClasses = args
            .map { param -> param::class.java }
            .toTypedArray()
        val internalMethod = mockk<Function<*>>() {}
        every { internalMethod.re } returns Integer::class.java
        every { internalMethod.parameterTypes } returns argClasses
        every { internalMethod.invoke(any(), args[0], args[1]) } answers { args.sum() }
internalMethod.para*/

        /*val instance = Any()
        val rpcMethod = RPCMethod(internalMethod, instance)
        val result = rpcMethod.invoke(argsAsProtoAny).unpack(PrimitiveType.Primitive::class.java)
        result.int shouldBe args.sum()

        verify(exactly = 1) {
            internalMethod.invoke(instance, args[0], args[1]);
        }*/

        val instance = MethodProvider()
        var result = RPCMethod(instance::idle, instance).invoke(emptyList())
        println("Result $result")
        result = RPCMethod(instance::ping, instance).invoke(emptyList())
        println("Result $result")
        var param = com.google.protobuf.Any.pack(PrimitiveType.Primitive.newBuilder().setString("StringValue").build())
        result = RPCMethod(instance::setValue, instance).invoke(listOf(param))
        println("Result $result")
        param = com.google.protobuf.Any.pack(RequestType.Request.getDefaultInstance())
        println("Result $result")

        /*val toManyArgs = arrayOf(1, 2, 3)
            .map { arg -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setInt(arg).build()) }
        assertThrows<CouldNotPerformException> { rpcMethod.invoke(toManyArgs) }*/
    }


}