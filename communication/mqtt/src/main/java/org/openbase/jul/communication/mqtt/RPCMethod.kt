package org.openbase.jul.communication.mqtt

import com.google.protobuf.Message
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive
import java.lang.Class
import java.lang.reflect.Method
import kotlin.Any
import kotlin.Boolean
import kotlin.Double
import kotlin.Float
import kotlin.Int
import kotlin.Long
import kotlin.String
import com.google.protobuf.Any as protoAny

class RPCMethod(private val method: Method, private val instance: Any) {

    companion object {
        /**
         * Create a function which converts an any type of the given class
         * to a proto any type.
         *
         * This only works for messages and certain java primitive types as
         * defined in the proto type Primitive.
         */
        fun anyToProtoAny(clazz: Class<*>): (Any) -> protoAny {
            if (Message::class.java.isAssignableFrom(clazz)) {
                return { msg: Any -> protoAny.pack(msg as Message) }
            }

            // Note: if a method is resolved via reflections Void::class.java does not match
            //       therefore, we perform a string comparison here
            if (clazz.name == "void") {
                return { _: Any -> protoAny.getDefaultInstance() }
            }

            return when (clazz) {
                Int::class.java, java.lang.Integer::class.java -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setInt(msg as Int).build());
                }
                Float::class.java, java.lang.Float::class.java -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setFloat(msg as Float).build())
                }
                Double::class.java, java.lang.Double::class.java -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setDouble(msg as Double).build())
                }
                Long::class.java, java.lang.Long::class.java -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setLong(msg as Long).build())
                }
                String::class.java -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setString(msg as String).build())
                }
                Boolean::class.java, java.lang.Boolean::class.java -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setBoolean(msg as Boolean).build());
                }

                else -> throw CouldNotPerformException("Cannot parse class ${clazz.name} to proto any!");
            }
        }

        /**
         * Create a function which converts a proto any type to a type
         * of the given class.
         *
         * This only works for messages and certain java primitive types as
         * defined in the proto type Primitive.
         */
        fun protoAnyToAny(clazz: Class<*>): (protoAny) -> Any {
            if (Message::class.java.isAssignableFrom(clazz)) {
                return { msg: protoAny -> msg.unpack(clazz as Class<Message>) }
            }

            return when (clazz) {
                Int::class.java, java.lang.Integer::class.java -> { msg: protoAny -> msg.unpack(Primitive::class.java).int }
                Float::class, java.lang.Float::class.java -> { msg: protoAny -> msg.unpack(Primitive::class.java).float }
                Double::class.java, java.lang.Double::class.java -> { msg: protoAny -> msg.unpack(Primitive::class.java).double }
                Long::class.java, java.lang.Long::class.java -> { msg: protoAny -> msg.unpack(Primitive::class.java).long }
                String::class.java -> { msg: protoAny -> msg.unpack(Primitive::class.java).string }
                Boolean::class.java, java.lang.Boolean::class.java -> { msg: protoAny -> msg.unpack(Primitive::class.java).boolean }

                else -> throw CouldNotPerformException("Cannot parse class ${clazz.name} from proto any!")
            }
        }
    }

    private val parameterParserList: List<(protoAny) -> Any> =
        method.parameterTypes.map { parameterType -> protoAnyToAny(parameterType) }
    private var resultParser: (Any) -> protoAny = anyToProtoAny(method.returnType)

    fun invoke(args: List<protoAny>): protoAny {
        if (args.size != parameterParserList.size) {
            throw CouldNotPerformException("Invalid number of arguments! Expected ${parameterParserList.size} but got ${args.size}")
        }

        val parameters = args
            .zip(parameterParserList)
            .map { (parameter, parameterParser) -> parameterParser(parameter) }
            .toTypedArray()
        val result = method.invoke(instance, *parameters)
        return resultParser(result);
    }
}
