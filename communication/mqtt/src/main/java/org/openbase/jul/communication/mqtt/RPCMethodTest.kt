package org.openbase.jul.communication.mqtt

import io.mockk.*
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.jupiter.api.assertThrows
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.mqtt.PrimitiveType
import org.openbase.type.communication.mqtt.ResponseType
import java.lang.reflect.Method

import com.google.protobuf.Any as protoAny

internal class RPCMethodTest {

    @Test
    fun `test anyToProtoAny and protoAnyToAny Conversion`() {
        fun <T: Any> backAndForth(value: T) : T {
            val anyToProtoAnyConverter = RPCMethod.anyToProtoAny(value::class.java)
            val protoAnyToAnyConverter = RPCMethod.protoAnyToAny(value::class.java)
            return protoAnyToAnyConverter(anyToProtoAnyConverter(value)) as T
        }

        val valueInt = 42
        assertEquals("Unexpected result for int conversion", valueInt, backAndForth(valueInt))

        val valueFloat = 3.141f
        assertEquals("Unexpected result for float conversion", valueFloat, backAndForth(valueFloat))

        val valueDouble = 2.781
        assertEquals("Unexpected result for double conversion", valueDouble, backAndForth(valueDouble), 0.01)

        val valueBoolean = true
        assertEquals("Unexpected result for double conversion", valueBoolean, backAndForth(valueBoolean))

        val valueString = "Hello"
        assertEquals("Unexpected result for double conversion", valueString, backAndForth(valueString))

        val valueMessage = ResponseType.Response.newBuilder().setStatus(ResponseType.Response.Status.ACKNOWLEDGED).build()
        assertEquals("Unexpected result for message conversion", valueMessage, backAndForth(valueMessage))

        assertThrows<CouldNotPerformException> { RPCMethod.anyToProtoAny(Any::class.java) }
        assertThrows<CouldNotPerformException> { RPCMethod.protoAnyToAny(Any::class.java) }
    }


    @Test
    fun `test invoke rpc method`() {
        val args = arrayOf(5, 6);
        val argsAsProtoAny = args
            .map {arg -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setInt(arg).build()) }
        val argClasses = args
            .map { param -> param::class.java }
            .toTypedArray()
        val internalMethod = mockk<Method>() {}
        every { internalMethod.returnType } returns Integer::class.java
        every { internalMethod.parameterTypes } returns argClasses
        every { internalMethod.invoke(any(), args[0], args[1]) } answers {args.sum()}

        val instance = Any()
        val rpcMethod = RPCMethod(internalMethod, instance)
        val result = rpcMethod.invoke(argsAsProtoAny).unpack(PrimitiveType.Primitive::class.java)
        assertEquals("Unexpected result of internal method call", args.sum(), result.int);

        verify(exactly = 1) {
            internalMethod.invoke(instance, args[0], args[1]);
        }

        val toManyArgs = arrayOf(1, 2, 3)
            .map {arg -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setInt(arg).build())}
        assertThrows<CouldNotPerformException> { rpcMethod.invoke(toManyArgs) }
    }


}