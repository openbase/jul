package org.openbase.jul.communication.mqtt

import com.google.protobuf.Any
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType
import org.openbase.type.communication.mqtt.PrimitiveType
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit
import kotlin.io.path.absolute
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeLines

@Testcontainers
class IntegrationTest {

    // the companion object makes sure that the container is started once before all tests instead of restarting for every test
    companion object {
        private const val port: Int = 1883

        private val mosquittoConfig = kotlin.io.path.createTempFile(prefix = "mosquitto_", suffix = ".conf")
        var broker: MqttBrokerContainer

        init {
            mosquittoConfig.writeLines(
                listOf(
                    "allow_anonymous true",
                    "listener 1883"
                )
            )

            broker = MqttBrokerContainer()
                .withExposedPorts(port)
                .withFileSystemBind(mosquittoConfig.absolute().toString(), "/mosquitto/config/mosquitto.conf", BindMode.READ_ONLY)
            broker.start()
        }

        @JvmStatic
        @AfterAll
        fun cleanup() {
            SharedMqttClient.waitForShutdown()
            broker.stop()
            mosquittoConfig.deleteIfExists()
        }
    }

    private val scope = ScopeProcessor.generateScope("/test/integration")
    private val config = CommunicatorConfig(broker.host, broker.firstMappedPort)

    internal class Adder {
        fun add(a: Int, b: Int): Int {
            return a + b
        }
    }

    @Test
    fun `test rpc over mqtt`() {
        val instance = Adder()

        val rpcServer = RPCServerImpl(scope, config)
        rpcServer.registerMethod(Adder::add, instance)
        rpcServer.activate()
        rpcServer.getActivationFuture()!!.get()

        val rpcClient = RPCClientImpl(scope, config)

        val expectedResult = 45
        val result = rpcClient.callMethod(instance::add.name, Int::class, 3, 42).get(1, TimeUnit.SECONDS)

        result shouldBe expectedResult
    }

    @Test
    @Timeout(value = 1, unit = TimeUnit.SECONDS)
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

class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("eclipse-mosquitto"))