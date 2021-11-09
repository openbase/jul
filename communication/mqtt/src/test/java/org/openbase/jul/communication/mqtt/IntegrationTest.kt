package org.openbase.jul.communication.mqtt

import com.google.protobuf.Any
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType
import org.openbase.type.communication.mqtt.PrimitiveType
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

@Testcontainers
class IntegrationTest {

    // the companion object makes sure that the container once before all tests instead of restarting for every test
    companion object {
        private const val port: Int = 1883
        private const val httpPort: Int = 8888

        @Container
        var broker: MqttBrokerContainer = MqttBrokerContainer()
            .withEnv("DOCKER_VERNEMQ_ACCEPT_EULA", "yes")
            .withEnv("DOCKER_VERNEMQ_LISTENER.tcp.allowed_protocol_versions", "5") // enable mqtt5
            .withEnv("DOCKER_VERNEMQ_ALLOW_ANONYMOUS", "on") // enable connection without password
            .withExposedPorts(port)
            .waitingFor(Wait.forHttp("/health").forPort(httpPort).forStatusCode(200));
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

class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("vernemq/vernemq"))