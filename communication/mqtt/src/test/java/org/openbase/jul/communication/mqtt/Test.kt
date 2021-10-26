package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import org.openbase.type.communication.mqtt.PrimitiveType
import org.openbase.type.communication.mqtt.RequestType
import java.util.*
import java.util.concurrent.TimeUnit
import com.google.protobuf.Any as protoAny

object Test {

    @JvmStatic
    fun main(args: Array<String>) {
        val v: Int = 42
        val asAny: Any = v

        println(RequestType.Request.getDefaultInstance())
        println(RequestType.Request.getDefaultInstance().descriptorForType)
        val build = RequestType.Request.newBuilder().setId("ID").build()
        val pack = protoAny.pack(build)

        val builder = PrimitiveType.Primitive.newBuilder()
        builder.int = v
        builder.setInt(asAny as Int)
        println("Is init ${builder.isInitialized}")
        println("Is init ${builder.build().isInitialized}")
        var packed = protoAny.pack(builder.build())

        /*var build = PrimitiveType.Primitive.newBuilder().setInt(42).build()
        println("Primitive as any: ${Any.pack(build)}")
        var pack = Any.pack(build).unpack(PrimitiveType.Primitive::class.java)
        println("Unpacked $pack")*/

        /*val client = MqttClient.builder()
            //.executorConfig(MqttClientExecutorConfig.builder().)
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.hivemq.com")
            .useMqttVersion5()
            .buildAsync()
        client.connect()[1, TimeUnit.SECONDS]
        println("Client connected!")

        val topic = "/asdkjaflasjdhalsk/test"
        val rpcServer = RPCServerImpl(client, topic)
        rpcServer.activate()
        val rpcRemote = RPCClientImpl(client, topic)
        println("Call method...")
        var get = rpcRemote.callMethod("testMethod3", String::class.java, 42).get()
        println("Method returned: $get")
        rpcServer.deactivate()

        client.disconnect();*/
    }
}