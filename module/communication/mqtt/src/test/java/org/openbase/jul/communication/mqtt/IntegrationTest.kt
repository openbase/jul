package org.openbase.jul.communication.mqtt

import com.google.protobuf.Any
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType
import org.openbase.type.communication.mqtt.PrimitiveType
import java.util.concurrent.TimeUnit

class IntegrationTest : AbstractIntegrationTest() {

    private val scope = ScopeProcessor.generateScope("/test/integration")
    private val config = CommunicatorConfig(brokerHost, brokerPort)

    internal class Adder {
        fun add(a: Int, b: Int): Int {
            return a + b
        }
    }

    @Test
    @Timeout(value = 30)
    fun `test rpc over mqtt`() {
        val instance = Adder()

        val rpcServer = RPCServerImpl(scope, config)
        rpcServer.registerMethod(Adder::add, instance)
        rpcServer.activate()
        rpcServer.getActivationFuture()!!.get()

        val rpcClient = RPCClientImpl(scope, config)

        val expectedResult = 45
        val result = rpcClient.callMethod(instance::add.name, Int::class, 3, 42).get(1, TimeUnit.SECONDS)

        result.response shouldBe expectedResult
    }

    @Test
    @Timeout(value = 30)
    fun `test pubsub over mqtt`() {
        val data = PrimitiveType.Primitive.newBuilder()
            .setString("IMPORTANT Message")
            .build()
        val expectedEvent = EventType.Event.newBuilder()
            .setPayload(Any.pack(data))
            .build()
        val lock = Object()

        val subscriber = SubscriberImpl(scope, config)
        subscriber.activate()
        subscriber.getActivationFuture()!!.get()
        subscriber.registerDataHandler { event ->
            event shouldBe expectedEvent
            synchronized(lock) {
                lock.notify()
            }
        }

        val publisher = PublisherImpl(scope, config)
        publisher.publish(expectedEvent)

        synchronized(lock) {
            lock.wait()
        }
    }
}
