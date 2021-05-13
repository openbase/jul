package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.openbase.jul.exception.CouldNotPerformException
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Future
import kotlin.Any
import com.google.protobuf.Any as protoAny

class RPCRemote(private val mqttClient: Mqtt5AsyncClient, topic: String) {

    private val topic: String = "$topic/rpc"

    private val parameterParserMap: HashMap<String, List<(Any) -> protoAny>> = HashMap()
    private val resultParserMap: HashMap<String, (protoAny) -> Any> = HashMap()

    fun <RETURN> callMethod(methodName: String, return_clazz: Class<RETURN>, vararg parameters: Any): Future<RETURN> {
        // lazily register parse methods for parameters and return type
        if (methodName !in resultParserMap) {
            resultParserMap[methodName] = protoAnyToAny(return_clazz)
        }
        val resultParser = resultParserMap[methodName]!!
        if (methodName !in parameterParserMap) {
            parameterParserMap[methodName] = parameters
                .map { param -> param::class.java }
                .map { param_clazz -> anyToProtoAny(param_clazz) }
        }
        val parameterParser = parameterParserMap[methodName]!!

        println("Build request")
        // create request
        val parametersProto = parameters
            .asList()
            .zip(parameterParser)
            .map { (param, parser) -> parser(param) }
        val request = RequestType.Request.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setMethodName(methodName)
            .addAllParams(parametersProto)
            .build()

        val future: CompletableFuture<RETURN> = CompletableFuture();

        println("Generate id ${request.id}")
        println("Subscribe to topic...")
        // subscribe to response topic
        mqttClient.subscribeWith()
            .topicFilter("$topic/${request.id}")
            .qos(MqttQos.EXACTLY_ONCE)
            .callback { mqtt5Publish: Mqtt5Publish ->
                val response = ResponseType.Response.parseFrom(mqtt5Publish.payloadAsBytes)
                if (response.status == ResponseType.Response.Status.FINISHED) {
                    // unsubscribe from topic
                    mqttClient.unsubscribeWith()
                        .topicFilter("$topic/${request.id}")
                        .send()

                    // handle response
                    handleResponse(response, future, resultParser)
                } else {
                    println("Received RPC Update ${response.status}")
                }
            }
            .send()
            .whenComplete { _, throwable ->
                if (throwable != null) {
                    future.completeExceptionally(throwable)
                } else {
                    println("Send request: $request to $topic")
                    mqttClient.publishWith()
                        .topic(topic)
                        .qos(MqttQos.EXACTLY_ONCE)
                        .payload(request.toByteArray())
                        .send()
                }
            }

        return future;
    }

    private fun <RETURN> handleResponse(
        response: ResponseType.Response,
        resultFuture: CompletableFuture<RETURN>,
        resultParser: (protoAny) -> Any
    ) {
        if (response.error.isNotEmpty()) {
            //TODO parse exception correctly
            resultFuture.completeExceptionally(CouldNotPerformException(response.error));
        } else {
            println("RPC call returned $response")
            resultFuture.complete(resultParser(response.result) as RETURN);
        }
    }

}