package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType
import org.openbase.type.communication.mqtt.ResponseType
import java.lang.reflect.Method

class RPCServer(private val mqttClient: Mqtt5AsyncClient, topic: String) {

    private val topic: String = "$topic/rpc"
    private var methods: HashMap<String, RPCMethod> = HashMap();

    fun activate() {
        for (method in this.javaClass.methods) {
            when (method.name) {
                "testMethod1" -> addMethod(method, this)
                "testMethod2" -> addMethod(method, this)
                "testMethod3" -> addMethod(method, this)
            }
        }

        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.EXACTLY_ONCE)
            .callback { mqtt5Publish -> handleRemoteCall(mqtt5Publish) }
            .executor(GlobalCachedExecutorService.getInstance().executorService)
            .send()
    }

    fun deactivate() {
        mqttClient.unsubscribeWith()
            .topicFilter(topic)
            .send() //TODO call get to make sure it is inactive?
    }

    fun testMethod1() {
        println("TestMethod1");
    }

    fun testMethod2(r: ResponseType.Response): String {
        println("Method 2 called")
        return r.error
    }

    fun testMethod3(r: Int): String {
        println("Method 3 called")
        return r.toString()
    }

    fun addMethod(method: Method, instance: Any) {
        methods[method.name] = RPCMethod(method, instance);
    }

    private fun handleRemoteCall(mqtt5Publish: Mqtt5Publish) {
        val responseBuilder = ResponseType.Response.newBuilder()
        val request = RequestType.Request.parseFrom(mqtt5Publish.payloadAsBytes)
        val requestTopic = topic + "/" + request.id

        println("Server received request\n$request")

        responseBuilder.status = ResponseType.Response.Status.ACKNOWLEDGED
        responseBuilder.id = request.id
        mqttClient.publishWith()
            .topic(requestTopic)
            .qos(MqttQos.EXACTLY_ONCE)
            .payload(responseBuilder.build().toByteArray())
            .send() //TODO call get?

        if (request.methodName !in methods) {
            responseBuilder.status = ResponseType.Response.Status.FINISHED;
            responseBuilder.error = "Method ${request.methodName} is not available";

            mqttClient.publishWith()
                .topic(requestTopic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(responseBuilder.build().toByteArray())
                .send();
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

        mqttClient.publishWith()
            .topic(requestTopic)
            .qos(MqttQos.EXACTLY_ONCE)
            .payload(responseBuilder.build().toByteArray())
            .send() //TODO call get?
    }
}