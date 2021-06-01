package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType
import org.openbase.type.communication.mqtt.ResponseType
import java.lang.reflect.Method
import java.util.*

class RPCServer(private val mqttClient: Mqtt5AsyncClient, topic: String) {

    private val topic: String = "$topic/rpc"
    private var methods: HashMap<String, RPCMethod> = HashMap();

    fun activate() {
        mqttClient.subscribe(
            Mqtt5Subscribe.builder()
                .topicFilter(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .build(),
            { mqtt5Publish -> handleRemoteCall(mqtt5Publish) },
            GlobalCachedExecutorService.getInstance().executorService
        )
    }

    fun deactivate() {
        mqttClient.unsubscribe(
            Mqtt5Unsubscribe.builder()
                .topicFilter(topic)
                .build()
        )//TODO call get to make sure it is inactive?
    }

    fun addMethod(method: Method, instance: Any) {
        methods[method.name] = RPCMethod(method, instance);
    }

    private fun handleRemoteCall(mqtt5Publish: Mqtt5Publish) {
        val responseBuilder = ResponseType.Response.newBuilder()
        val request = RequestType.Request.parseFrom(mqtt5Publish.payloadAsBytes)

        // make sure that the request id is a valid uuid so that request
        // collisions are unlikely
        responseBuilder.id = UUID.fromString(request.id).toString()
        val requestTopic = topic + "/" + responseBuilder.id
        val mqttResponseBuilder = Mqtt5Publish.builder()
            .topic(requestTopic)
            .qos(MqttQos.EXACTLY_ONCE)

        println("Server received request\n$request")

        responseBuilder.status = ResponseType.Response.Status.ACKNOWLEDGED
        responseBuilder.id = request.id
        mqttClient.publish(
            mqttResponseBuilder
                .payload(responseBuilder.build().toByteArray())
                .build()
        ) //TODO call get?

        if (request.methodName !in methods) {
            responseBuilder.status = ResponseType.Response.Status.FINISHED;
            responseBuilder.error = "Method ${request.methodName} is not available";

            mqttClient.publish(
                mqttResponseBuilder
                    .payload(responseBuilder.build().toByteArray())
                    .build()
            ) //TODO call get?
            return;
        }

        val method = methods[request.methodName]!!;

        //TODO open thread to send PROGRESSING message periodically
        try {
            val result = method.invoke(request.paramsList);
            println("Internal invoke returned $result");
            responseBuilder.status = ResponseType.Response.Status.FINISHED
            responseBuilder.result = result
        } catch (ex: Exception) {
            responseBuilder.status = ResponseType.Response.Status.FINISHED
            responseBuilder.error = ex.message //TODO: build message accordingly from stackstrace...
        }
        println("Return response: ${responseBuilder.build()}")

        mqttClient.publish(
            mqttResponseBuilder
                .payload(responseBuilder.build().toByteArray())
                .build()
        ) //TODO call get?
    }
}