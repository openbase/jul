package org.openbase.jul.communication.mqtt

import com.google.protobuf.Any
import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttClientExecutorConfig
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        /*var build = PrimitiveType.Primitive.newBuilder().setInt(42).build()
        println("Primitive as any: ${Any.pack(build)}")
        var pack = Any.pack(build).unpack(PrimitiveType.Primitive::class.java)
        println("Unpacked $pack")*/

        val client = MqttClient.builder()
            //.executorConfig(MqttClientExecutorConfig.builder().)
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.hivemq.com")
            .useMqttVersion5()
            .buildAsync()
        client.connect()[1, TimeUnit.SECONDS]

        val topic = "/asdkjaflasjdhalsk/test"
        val rpcServer = RPCServer(client, topic)
        rpcServer.activate()
        val rpcRemote = RPCRemote(client, topic)
        println("Call method...")
        var get = rpcRemote.callMethod("testMethod3", String::class.java, 42).get()
        println("Method returned: $get")
        rpcServer.deactivate()

        client.disconnect();
    }
}