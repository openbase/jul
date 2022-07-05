package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import io.mockk.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType.Event
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive
import java.util.concurrent.CompletableFuture
import com.google.protobuf.Any as protoAny

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class PublisherImpTest {

    private val mqttClient: Mqtt5AsyncClient = mockk()

    init {
        mockkObject(SharedMqttClient)
        every { SharedMqttClient.get(any()) } returns mqttClient

        every { mqttClient.publish(any()) } returns CompletableFuture()
    }

    @AfterAll
    fun clearMocks() {
        clearAllMocks()
    }

    @Test
    @Timeout(value = 30)
    fun `test publish`() {
        val expectedData = Primitive.newBuilder()
            .setString("Hello World")
            .build()
        val expectedEvent = Event.newBuilder()
            .setPayload(protoAny.pack(expectedData))
            .build()

        val topic = "/test/publish"
        val publisher = PublisherImpl(ScopeProcessor.generateScope(topic), CommunicatorConfig("localhost", 1234))

        publisher.publish(expectedData, false)

        verify(exactly = 1) {
            mqttClient.publish(
                Mqtt5Publish.builder()
                    .topic(topic)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(expectedEvent.toByteArray())
                    .build()
            )
        }
    }
}
