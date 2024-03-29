package org.openbase.jul.communication.mqtt

import com.google.protobuf.Message
import com.google.protobuf.ProtocolMessageEnum
import org.openbase.jul.annotation.RPCMethod
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
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
import kotlin.reflect.full.staticFunctions
import com.google.protobuf.Any as protoAny

/**
 * Wrap a function to be callable as a remote procedure call (RPC).
 * To do this, the arguments and the return value of the function
 * are wrapped in the [protobuf any type][com.google.protobuf.Any].
 * As a result, arguments and return values have to either be protobuf
 * messages or certain primitive values as defined in the type
 * [Primitive][org.openbase.type.communication.mqtt.PrimitiveType.Primitive].
 *
 * Note: the instance parameter is only required if the function is resolved
 * from a class (Class::function) and not from an instance of the class (instance::function).
 * In the first case the function expects the first argument to be the instance,
 * in the second case the function knows of the instance to be called on.
 *
 * @param function the function to be wrapped
 * @param instance the instance the function is called on, is only required
 */
class RPCMethodWrapper(private val function: KFunction<*>, val priority: RPCMethod.Priority = RPCMethod.Priority.NORMAL, private val instance: Any = noInstance) {

    companion object {
        /**
         * Value indicating that no instance is required to call the
         * wrapped function.
         */
        private const val noInstance = false

        /**
         * Create a function which converts an any type of the given class
         * to a proto any type.
         *
         * This only works for messages, message enums and certain primitive types as
         * defined in [org.openbase.type.communication.mqtt.PrimitiveType.Primitive].
         */
        fun anyToProtoAny(clazz: KClass<*>): (Any) -> protoAny {
            if (Message::class.isSuperclassOf(clazz)) {
                return { msg: Any -> protoAny.pack(msg as Message) }
            }

            if (ProtocolMessageEnum::class.isSuperclassOf(clazz)) {
                return { msg: Any ->
                    val asEnum = msg as ProtocolMessageEnum
                    protoAny.pack(Primitive.newBuilder().setInt(asEnum.number).build())
                }
            }

            if (clazz.java == java.lang.Void::class.java) {
                return { _ -> protoAny.getDefaultInstance() }
            }

            return when (clazz) {
                Int::class -> { msg: Any ->
                    protoAny.pack(Primitive.newBuilder().setInt(msg as Int).build())
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
                Unit::class -> { _ -> protoAny.getDefaultInstance() }

                else -> throw CouldNotPerformException("Cannot parse class ${clazz.qualifiedName} to proto any!");
            }
        }

        /**
         * Create a function which converts a proto any type to a type
         * of the given class.
         *
         * This only works for messages, message enums and certain primitive types as
         * defined in [org.openbase.type.communication.mqtt.PrimitiveType.Primitive].
         */
        fun protoAnyToAny(clazz: KClass<*>): (protoAny) -> Any {
            if (Message::class.isSuperclassOf(clazz)) {
                return { msg: protoAny -> msg.unpack(clazz.java as Class<Message>) }
            }

            if (ProtocolMessageEnum::class.isSuperclassOf(clazz)) {
                val forNumberFunction = clazz.staticFunctions.first { function -> function.name == "forNumber" }
                return { msg ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasInt()) {
                        throw CouldNotPerformException("Arg is not of type Int which is needed to unpack enums!")
                    }
                    forNumberFunction.call(unpacked.int)
                        ?: throw CouldNotPerformException("${unpacked.int} is an invalid number for enum ${clazz.simpleName}")
                }
            }

            if (clazz.java == java.lang.Void::class.java) {
                return { _ -> }
            }

            return when (clazz) {
                Int::class -> { msg: protoAny ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasInt()) {
                        throw CouldNotPerformException("Arg is not of type Int!")
                    }
                    unpacked.int
                }
                Float::class -> { msg: protoAny ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasFloat()) {
                        throw CouldNotPerformException("Arg is not of type Float!")
                    }
                    unpacked.float
                }
                Double::class -> { msg: protoAny ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasDouble()) {
                        throw CouldNotPerformException("Arg is not of type Double!")
                    }
                    unpacked.double
                }
                Long::class -> { msg: protoAny ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasLong()) {
                        throw CouldNotPerformException("Arg is not of type Long!")
                    }
                    unpacked.long
                }
                String::class -> { msg: protoAny ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasString()) {
                        throw CouldNotPerformException("Arg is not of type String!")
                    }
                    unpacked.string
                }
                Boolean::class -> { msg: protoAny ->
                    val unpacked: Primitive = msg.unpack(Primitive::class.java)
                    if (!unpacked.hasBoolean()) {
                        throw CouldNotPerformException("Arg is not of type Boolean!")
                    }
                    unpacked.boolean
                }
                Unit::class -> { _ -> }

                else -> throw CouldNotPerformException("Cannot parse class ${clazz.qualifiedName} from proto any!")
            }
        }
    }

    private val callOnInstance = instance != noInstance

    private var futureMethod = function.returnType.classifier == Future::class
    private var resultParser: (Any) -> protoAny
    init {
        try {
            resultParser = if (futureMethod) {
                anyToProtoAny(function.returnType.arguments[0].type!!.classifier as KClass<*>)
            } else {
                anyToProtoAny(function.returnType.classifier as KClass<*>)
            }
        } catch (ex: CouldNotPerformException) {
            throw CouldNotPerformException("Could not register method ${function.name}", ex)
        }
    }

    private var parameterParserList: List<(protoAny) -> Any>

    init {
        var filteredParameters = function.parameters

        // if called on an instance the first parameter of the function is the
        // instance and, it does not need to be wrapped
        if (callOnInstance) {
            filteredParameters = filteredParameters.subList(1, filteredParameters.size)
        }

        try {
            parameterParserList = filteredParameters
                .map { parameter -> protoAnyToAny(parameter.type.classifier as KClass<*>) }
        } catch (ex: CouldNotPerformException) {
            throw CouldNotPerformException("Could not register method ${function.name}", ex)
        }
    }

    fun invoke(args: List<protoAny>): protoAny {
        if (args.size != parameterParserList.size) {
            throw CouldNotPerformException("Invalid number of arguments! Expected ${parameterParserList.size} but got ${args.size}")
        }

        var parameters = args
            .zip(parameterParserList)
            .map { (parameter, parameterParser) -> parameterParser(parameter) }
            .toTypedArray()
        // prepend instance if necessary for the function call
        if (callOnInstance) {
            parameters = arrayOf(instance, *parameters)
        }

        var result = function.call(*parameters)!!
        if (futureMethod) {
            //TODO: make timeout accessible from RPCUtils?
            try {
                result = (result as Future<*>).get(5, TimeUnit.MINUTES)
            } catch (ex: TimeoutException) {
                (result as Future<*>).cancel(true)
                throw ex
            }
        }
        return resultParser(result);
    }
}
