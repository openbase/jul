package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType.Request
import org.openbase.type.communication.mqtt.ResponseType.Response
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
        lazyRegisterMethod(methodName, return_clazz, *parameters)

        val request = generateRequest(methodName, *parameters)
        val rpcFuture: CompletableFuture<RETURN> = CompletableFuture();

        mqttClient.subscribe(
            Mqtt5Subscribe.builder()
                .topicFilter("$topic/${request.id}")
                .qos(MqttQos.EXACTLY_ONCE)
                .build(),
            { mqtt5Publish: Mqtt5Publish -> handleRPCResponse(mqtt5Publish, rpcFuture, request) },
            GlobalCachedExecutorService.getInstance().executorService
        ).whenComplete { _, throwable ->
            if (throwable != null) {
                rpcFuture.completeExceptionally(throwable)
            } else {
                mqttClient.publish(
                    Mqtt5Publish.builder()
                        .topic(topic)
                        .qos(MqttQos.EXACTLY_ONCE)
                        .payload(request.toByteArray())
                        .build()
                )
            }
        }

        return rpcFuture
    }

    private fun <RETURN> handleRPCResponse(
        mqtt5Publish: Mqtt5Publish,
        rpcFuture: CompletableFuture<RETURN>,
        request: Request
    ) {
        val response = Response.parseFrom(mqtt5Publish.payloadAsBytes)
        if (response.error.isEmpty() && response.status != Response.Status.FINISHED) {
            //TODO update timeout for coroutine which checks if server is still active
            return
        }

        mqttClient.unsubscribe(
            Mqtt5Unsubscribe.builder()
                .topicFilter("$topic/${request.id}")
                .build()
        )

        if (response.error.isNotEmpty()) {
            //TODO parse exception correctly
            rpcFuture.completeExceptionally(CouldNotPerformException(response.error));
        } else {
            rpcFuture.complete(resultParserMap[request.methodName]!!(response.result) as RETURN);
        }
    }

    private fun lazyRegisterMethod(methodName: String, return_clazz: Class<*>, vararg parameters: Any) {
        resultParserMap.getOrPut(methodName) { RPCMethod.protoAnyToAny(return_clazz) }
        parameterParserMap.getOrPut(methodName) {
            parameters
                .map { param -> param::class.java }
                .map { param_clazz -> RPCMethod.anyToProtoAny(param_clazz) }
        }
    }

    fun generateRequestId(): String {
        return UUID.randomUUID().toString()
    }

    private fun generateRequest(methodName: String, vararg parameters: Any): Request {
        return Request.newBuilder()
            .setId(generateRequestId())
            .setMethodName(methodName)
            .addAllParams(parameters
                .asList()
                .zip(parameterParserMap[methodName]!!)
                .map { (param, parser) -> parser(param) })
            .build()
    }
}