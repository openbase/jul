package org.openbase.jul.communication.mqtt

import com.google.protobuf.Message
import org.openbase.jul.exception.CouldNotPerformException
import com.google.protobuf.Any as protoAny
import java.lang.reflect.Method
import java.util.*

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

    //TODO: how can you check if a class is void?
    if (clazz.name == "void") {
        return { _: Any -> protoAny.getDefaultInstance()}
    }

    return when (clazz) {
        Integer::class.java -> { msg: Any -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setInt(msg as Int).build()); }
        Float::class.java -> { msg: Any -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setFloat(msg as Float).build()); }
        Double::class.java -> { msg: Any -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setDouble(msg as Double).build()); }
        Long::class.java -> { msg: Any -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setLong(msg as Long).build()); }
        String::class.java -> { msg: Any -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setString(msg as String).build()); }
        Boolean::class.java -> { msg: Any -> protoAny.pack(PrimitiveType.Primitive.newBuilder().setBoolean(msg as Boolean).build()); }


        else -> throw Exception("Cannot parse class ${clazz.name} into proto any!");
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
        Int::class.java -> { msg: protoAny -> msg.unpack(PrimitiveType.Primitive::class.java).int; }
        Float::class.java -> { msg: protoAny -> msg.unpack(PrimitiveType.Primitive::class.java).float; }
        Double::class.java -> { msg: protoAny -> msg.unpack(PrimitiveType.Primitive::class.java).double; }
        Long::class.java -> { msg: protoAny -> msg.unpack(PrimitiveType.Primitive::class.java).long; }
        String::class.java -> { msg: protoAny -> msg.unpack(PrimitiveType.Primitive::class.java).string; }
        Boolean::class.java -> { msg: protoAny -> msg.unpack(PrimitiveType.Primitive::class.java).boolean; }

        else -> throw CouldNotPerformException("Cannot parse class ${clazz.name} from proto any!")
    }
}

class RPCMethod(private val method: Method, private val instance: Any) {

    private val parameterParserList: List<(protoAny) -> Any> = method.parameterTypes.map { parameterType -> protoAnyToAny(parameterType) }
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
