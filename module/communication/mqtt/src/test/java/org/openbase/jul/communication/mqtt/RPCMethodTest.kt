package org.openbase.jul.communication.mqtt

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive
import org.openbase.type.communication.mqtt.ResponseType
import org.openbase.type.domotic.unit.UnitTemplateType
import kotlin.Any
import com.google.protobuf.Any as protoAny

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RPCMethodTest {

    @Test
    @Timeout(value = 30)
    fun `test anyToProtoAny and protoAnyToAny Conversion`() {
        fun <T : Any> backAndForth(value: T): T {
            val anyToProtoAnyConverter = RPCMethod.anyToProtoAny(value::class)
            val protoAnyToAnyConverter = RPCMethod.protoAnyToAny(value::class)
            return protoAnyToAnyConverter(anyToProtoAnyConverter(value)) as T
        }

        // test primitives
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

        val valueUnit = Unit
        backAndForth(valueUnit) shouldBe valueUnit

        // test messages
        val valueMessage =
            ResponseType.Response.newBuilder().setStatus(ResponseType.Response.Status.ACKNOWLEDGED).build()
        backAndForth(valueMessage) shouldBe valueMessage

        // test message eum
        val valueEnum = UnitTemplateType.UnitTemplate.UnitType.DEVICE
        backAndForth(valueEnum) shouldBe valueEnum

        // test exceptions on other types
        shouldThrow<CouldNotPerformException> { RPCMethod.anyToProtoAny(Any::class) }
        shouldThrow<CouldNotPerformException> { RPCMethod.protoAnyToAny(Any::class) }
    }

    internal class FunctionProvider(val returnValue: Double = 43.0) {
        var receivedArg : Double = 0.0

        fun `no args no return`() {}
        fun `no args return`(): Double = returnValue
        fun `args no return`(arg: Double) { receivedArg = arg }
        fun `args return`(value: Double): Double = value + returnValue
    }

    @Test
    @Timeout(value = 30)
    fun `test invoke rpc method`() {
        val returnValue = 68.32
        val arg = 21.76
        val argProto: protoAny = protoAny.pack(Primitive.newBuilder().setDouble(arg).build())
        val instance = FunctionProvider(returnValue = returnValue)

        RPCMethod(instance::`no args no return`).invoke(emptyList()) shouldBe protoAny.getDefaultInstance()

        var result: protoAny = RPCMethod(instance::`no args return`).invoke(emptyList())
        result.unpack(Primitive::class.java).double shouldBe returnValue

        RPCMethod(instance::`args no return`).invoke(listOf(argProto)) shouldBe protoAny.getDefaultInstance()
        instance.receivedArg shouldBe arg

        result = RPCMethod(instance::`args return`).invoke(listOf(argProto))
        result.unpack(Primitive::class.java).double shouldBe returnValue + arg
    }

    @Test
    @Timeout(value = 30)
    fun `test invoke rpc method with class function`() {
        val instance = FunctionProvider()

        // correct use with class function
        RPCMethod(FunctionProvider::`no args no return`, instance).invoke(emptyList()) shouldBe protoAny.getDefaultInstance()

        // class function but no instance provided should throw an error
        shouldThrow<CouldNotPerformException> { RPCMethod(FunctionProvider::`no args no return`).invoke(emptyList()) }
    }

    @Test
    @Timeout(value = 30)
    fun `test invoke rpc method errors`() {
        val instance = FunctionProvider()

        // wrong type of arg
        val wrongTypeArg: protoAny = protoAny.pack(Primitive.newBuilder().setString("WRONG").build())
        shouldThrow<CouldNotPerformException> { RPCMethod(instance::`args no return`).invoke(listOf(wrongTypeArg)) }

        // wrong number of args
        val toManyArg: protoAny = protoAny.pack(Primitive.newBuilder().setDouble(42.0).build())
        shouldThrow<CouldNotPerformException> { RPCMethod(instance::`no args no return`).invoke(listOf(toManyArg)) }
        shouldThrow<CouldNotPerformException> { RPCMethod(instance::`args no return`).invoke(emptyList()) }
    }

}
