package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import java.util.*
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

object Test {
    @Throws(ExecutionException::class, InterruptedException::class)
    @JvmStatic
    fun main(args: Array<String>) {
        val client = MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost("broker.hivemq.com")
            .useMqttVersion5()
            .buildAsync()
        try {
            client.connect()[1, TimeUnit.SECONDS]
        } catch (e: InterruptedException) {
            e.printStackTrace()
        } catch (e: TimeoutException) {
            println("Connection failed")
            e.printStackTrace()
        } catch (e: ExecutionException) {
            println("Connection failed")
            e.printStackTrace()
        }
        val topic = "/asdkjaflasjdhalsk/test"
        val rpcServer = RPCServer(client, topic)
        rpcServer.activate()
        val rpcRemote = RPCRemote(client, topic)
        println("Call method")
        rpcRemote.callMethod("testMethod2")
        println("Wait...")
        Thread.sleep(3000)
        println("Finished")
        rpcServer.deactivate()
    }
}