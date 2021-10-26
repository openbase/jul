package org.openbase.jul.communication.mqtt

import com.google.protobuf.Message
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive
import java.lang.Class
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import kotlin.reflect.KClass
import kotlin.reflect.KFunction
import kotlin.reflect.full.isSuperclassOf
import com.google.protobuf.Any as protoAny

class RPCMethod(private val function: KFunction<*>, private val instance: Any) {

    companion object {
        /**
         * Create a function which converts an any type of the given class
         * to a proto any type.
         *
         * This only works for messages and certain java primitive types as
         * defined in the proto type Primitive.
         */
        fun anyToProtoAny(clazz: KClass<*>): (Any) -> protoAny {
            println("Create converter for ${clazz.qualifiedName} to protoAny...")

            if (Message::class.isSuperclassOf(clazz)) {
                return { msg: Any -> protoAny.pack(msg as Message) }
            }

            // Note: if a method is resolved via reflections Void::class.java does not match
            //       therefore, we perform a string comparison here
            /*if (clazz.qualifiedName == "void") {
                return { _: Any? -> protoAny.getDefaultInstance() }
            }*/

            return when (clazz) {
                Int::class -> { msg: Any ->
                    println("Call converter for int class with $msg")
                    println("Cast ${msg as Int}")
                    val b = Primitive.newBuilder()
                    b.int = msg as Int
                    print("Hallo?")
                    print("Builder ${b.isInitialized} msg ${b.build().isInitialized}")
                    //println("As primitive ${Primitive.newBuilder().setInt(msg as Int).build()}")
                    protoAny.pack(b.build())
                }
                Float::class -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setFloat(msg as Float).build())
                }
                Double::class -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setDouble(msg as Double).build())
                }
                Long::class -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setLong(msg as Long).build())
                }
                String::class -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setString(msg as String).build())
                }
                Boolean::class -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setBoolean(msg as Boolean).build());
                }
                Unit::class -> {_ -> protoAny.getDefaultInstance()}

                else -> throw CouldNotPerformException("Cannot parse class ${clazz.qualifiedName} to proto any!");
            }
        }

        /**
         * Create a function which converts a proto any type to a type
         * of the given class.
         *
         * This only works for messages and certain java primitive types as
         * defined in the proto type Primitive.
         */
        fun protoAnyToAny(clazz: KClass<*>): (protoAny) -> Any {
            if (Message::class.isSuperclassOf(clazz)) {
                return { msg: protoAny -> msg.unpack(clazz.java as Class<Message>) }
            }

            return when (clazz) {
                Int::class -> { msg: protoAny -> msg.unpack(Primitive::class.java).int }
                Float::class -> { msg: protoAny -> msg.unpack(Primitive::class.java).float }
                Double::class -> { msg: protoAny -> msg.unpack(Primitive::class.java).double }
                Long::class -> { msg: protoAny -> msg.unpack(Primitive::class.java).long }
                String::class -> { msg: protoAny -> msg.unpack(Primitive::class.java).string }
                Boolean::class -> { msg: protoAny -> msg.unpack(Primitive::class.java).boolean }
                Unit::class -> {_ -> protoAny.getDefaultInstance()}

                else -> throw CouldNotPerformException("Cannot parse class ${clazz.qualifiedName} from proto any!")
            }
        }
    }

    private val parameterParserList: List<(protoAny) -> Any> =
        function.parameters.map { parameter -> protoAnyToAny(parameter.type.classifier as KClass<*>) }
    private var resultParser: (Any) -> protoAny = anyToProtoAny(function.returnType.classifier as KClass<*>)

    fun invoke(args: List<protoAny>): protoAny {
        if (args.size != parameterParserList.size) {
            throw CouldNotPerformException("Invalid number of arguments! Expected ${parameterParserList.size} but got ${args.size}")
        }

        val parameters = args
            .zip(parameterParserList)
            .map { (parameter, parameterParser) -> parameterParser(parameter) }
            //.filter { parameter -> parameter == protoAny.getDefaultInstance() }
            .toTypedArray()

        println("Got ${parameters.size}")
        parameters.forEach { arg -> println(arg) }

        val result = function.call(instance, *parameters)!!
        return resultParser(result);
    }
}
