package org.openbase.jul.communication.mqtt

import com.google.protobuf.ByteString
import com.google.protobuf.Message
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import java.lang.reflect.Method
import java.nio.charset.StandardCharsets
import java.util.concurrent.CompletableFuture

class RPCServer(private val mqttClient: Mqtt5AsyncClient, topic: String) {
    private val topic: String = "$topic/rpc"

    //private val activationFuture: CompletableFuture<Mqtt5SubAck>? = null;

    //private val active get() = activationFuture != null && activationFuture.isDone

    private var methods: HashMap<String, Method> = HashMap();

    fun activate() {
        println("Register methods")
        for (method in this.javaClass.methods) {
            println("Register ${method.name}")
            when (method.name) {
                "testMethod1" -> addMethod(method.name, method)
                "testMethod2" -> addMethod(method.name, method)
                "testMethod3" -> addMethod(method.name, method)
            }
        }

        mqttClient.subscribeWith()
            .topicFilter(topic)
            .qos(MqttQos.EXACTLY_ONCE)
            .callback {
                handleRemoteCall(it)
            }
            .send() //TODO call get to make sure it is active?
    }

    fun deactivate() {
        mqttClient.unsubscribeWith()
            .topicFilter(topic)
            .send() //TODO call get to make sure it is inactive?
    }

    fun testMethod1() {
        println("TestMethod1");
    }

    fun testMethod2(r : ResponseType.Response) : String {
        println("Method 2 called")
        return r.error
    }

    fun testMethod3(r : Int) : String {
        println("Method 3 called")
        return r.toString()
    }

    fun addMethod(methodName: String, method: Method) {
        for (parameterType in method.parameterTypes) {
            println(parameterType.isPrimitive)
            //TODO: also handle primitives
            if (!Message::class.java.isAssignableFrom(parameterType)) {
                println("Could not register $methodName because ${parameterType.name}");
            }
        }
        //TODO: warn if already registered
        methods[methodName] = method;
    }

    private fun handleRemoteCall(mqtt5Publish: Mqtt5Publish) {
        val responseBuilder = ResponseType.Response.newBuilder()
        val request = RequestType.Request.parseFrom(mqtt5Publish.payloadAsBytes)
        val requestTopic = topic + "/" + request.id

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

        if (request.paramsCount != method.parameterTypes.size) {
            responseBuilder.status = ResponseType.Response.Status.FINISHED;
            responseBuilder.error = "\"Not enough parameters provided\"";

            mqttClient.publishWith()
                .topic(requestTopic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(responseBuilder.build().toByteArray())
                .send();
            return;
        }

        println("Method parameter count ${method?.parameterTypes?.size}")

        var args = request.paramsList
            .zip(method.parameterTypes)
            .map {
            it.second.getMethod("parseFrom", ByteString::class.java).invoke(null, it.first) }

        var invoke = method.invoke(null, args) as Message

        responseBuilder.result = invoke.toByteString();

        //TODO open thread to send PROGRESSING message periodically
        try {
            //TODO call method as defined in request.getMethodName();
            request.methodName
            responseBuilder.status = ResponseType.Response.Status.FINISHED
            responseBuilder.result = ByteString.copyFrom(request.methodName.toByteArray(StandardCharsets.UTF_8))
        } catch (ex: Exception) {
            responseBuilder.status = ResponseType.Response.Status.FINISHED
            responseBuilder.error = ex.message //TODO: build message accordingly from stackstrace...
        }
        mqttClient.publishWith()
            .topic(requestTopic)
            .qos(MqttQos.EXACTLY_ONCE)
            .payload(responseBuilder.build().toByteArray())
            .send() //TODO call get?
    }
}