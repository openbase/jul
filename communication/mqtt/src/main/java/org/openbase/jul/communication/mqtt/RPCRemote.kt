package org.openbase.jul.communication.mqtt

import com.google.protobuf.InvalidProtocolBufferException
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import java.util.concurrent.ExecutionException
import java.lang.InterruptedException
import java.util.UUID
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.openbase.jul.communication.mqtt.ResponseType

class RPCRemote(private val mqttClient: Mqtt5AsyncClient, topic: String) {

    private val topic: String = "$topic/rpc"

    @Throws(ExecutionException::class, InterruptedException::class)
    fun callMethod(methodName: String?) {
        val id = UUID.randomUUID().toString()
        println("Generate id $id")
        println("Subscribe to topic...")
        mqttClient.subscribeWith()
            .topicFilter("$topic/$id")
            .qos(MqttQos.EXACTLY_ONCE)
            .callback { mqtt5Publish: Mqtt5Publish ->
                var response: ResponseType.Response? = null
                try {
                    response = ResponseType.Response.parseFrom(mqtt5Publish.payloadAsBytes)
                } catch (e: InvalidProtocolBufferException) {
                    e.printStackTrace()
                }
                println("Received response: " + response!!.status.name)
                println("Error: ${response.error}" )
            }.send().get()
        println("Build request")
        val requestBuilder = RequestType.Request.newBuilder()
        requestBuilder.id = id
        requestBuilder.methodName = methodName
        requestBuilder.addParams(RequestType.Request.newBuilder().setId("12345").build().toByteString());
        //TODO set params
        println("Send request: " + requestBuilder.build() + " to " + topic)
        mqttClient.publishWith()
            .topic(topic)
            .qos(MqttQos.EXACTLY_ONCE)
            .payload(requestBuilder.build().toByteArray())
            .send().get()

        //TODO handle methods correctly
        //TODO return Future related to the callback above
        //TODO unsubscribe after function finished
    }

}