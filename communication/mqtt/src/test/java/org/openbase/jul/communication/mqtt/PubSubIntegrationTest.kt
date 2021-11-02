package org.openbase.jul.communication.mqtt

import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import com.google.protobuf.Any as protoAny
import org.junit.jupiter.api.Test
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType
import org.openbase.type.communication.mqtt.PrimitiveType
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class PubSubIntegrationTest {

    // the companion object makes sure that the container once before all tests instead of restarting for every test
    companion object {
        private const val port: Int = 1883

        @Container
        var broker: MqttBrokerContainer = MqttBrokerContainer()
            .withEnv("DOCKER_VERNEMQ_ACCEPT_EULA", "yes")
            .withEnv("DOCKER_VERNEMQ_LISTENER.tcp.allowed_protocol_versions", "5") // enable mqtt5
            .withEnv("DOCKER_VERNEMQ_ALLOW_ANONYMOUS", "on") // enable connection without password
            .withExposedPorts(port)
    }

    private val scope = ScopeProcessor.generateScope("/test/pubsub")
    private val config = CommunicatorConfig(broker.host, broker.firstMappedPort)

    @Test
    fun test() {
        val data = PrimitiveType.Primitive.newBuilder()
            .setString("IMPORTANT Message")
            .build()
        val expectedEvent = EventType.Event.newBuilder()
            .setPayload(protoAny.pack(data))
            .build()
        var notifications = 0

        val subscriber = SubscriberImpl(scope, config)
        subscriber.activate()
        subscriber.getActivationFuture()!!.get()
        subscriber.registerDataHandler { event ->  {
            notifications++;
            event shouldBe expectedEvent
        } }

        val publisher = PublisherImpl(scope, config)
        publisher.publish(expectedEvent)

        //TODO: how to wait the right time here? wait on event with timeout? polling?
        Thread.sleep(100)
        notifications shouldBe 1
    }
}