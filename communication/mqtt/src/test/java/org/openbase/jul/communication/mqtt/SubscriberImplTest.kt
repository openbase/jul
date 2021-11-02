package org.openbase.jul.communication.mqtt

import com.google.protobuf.Any as protoAny
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.EventType.Event
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SubscriberImplTest {

    private val mqttClient: Mqtt5AsyncClient = mockk()

    init {
        mockkObject(SharedMqttClient)
        every { SharedMqttClient.get(any()) } returns mqttClient

        every { mqttClient.publish(any()) } returns CompletableFuture()
    }

    /**
     * Capture the callback which is invoked if an
     * Event is received.
     */


    @AfterAll
    fun clearMocks() {
        clearAllMocks()
    }

    @BeforeEach
    fun initMqttClientMock() {
        clearMocks(mqttClient)
    }

    private val topic = "/test/subscribe"
    private val scope = ScopeProcessor.generateScope(topic)
    private val config = CommunicatorConfig("localhost", 1873)
    val subscriber = SubscriberImpl(scope, config)

    @Test
    fun `test activate`() {
        val callbackSlot = slot<Consumer<Mqtt5Publish>>()
        every {
            mqttClient.subscribe(
                any(),
                capture(callbackSlot),
                GlobalCachedExecutorService.getInstance().executorService
            )
        } returns CompletableFuture()

        subscriber.activate()

        subscriber.getActivationFuture() shouldNotBe null
        verify(exactly = 1) {
            mqttClient.subscribe(
                Mqtt5Subscribe.builder()
                    .topicFilter(topic)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .build(),
                callbackSlot.captured,
                GlobalCachedExecutorService.getInstance().executorService
            )
        }
    }

    @Test
    fun `test deactivate`() {
        every { mqttClient.unsubscribe(any()) } returns CompletableFuture()

        subscriber.deactivate()

        subscriber.getActivationFuture() shouldBe null
        verify(exactly = 1) {
            mqttClient.unsubscribe(
                Mqtt5Unsubscribe.builder()
                    .topicFilter(topic)
                    .build()
            )
        }
    }

    @Nested
    inner class TestHandlerManagement {

        // capture the callback invoked if the subscriber receives an event
        private val callbackSlot = slot<Consumer<Mqtt5Publish>>()
        private val callback: Consumer<Mqtt5Publish>

        init {
            // mock
            every {
                mqttClient.subscribe(
                    any(),
                    capture(callbackSlot),
                    GlobalCachedExecutorService.getInstance().executorService
                )
            } returns CompletableFuture()

            // capture callback
            subscriber.activate()
            callback = callbackSlot.captured
        }

        @Test
        fun `test handler management`() {
            val payload = Primitive.newBuilder()
                .setString("Payload")
                .build()
            val event = Event.newBuilder()
                .setPayload(protoAny.pack(payload))
                .build()
            val receivedEvents: MutableList<Event> = mutableListOf()

            // register handler
            val handlerId = subscriber.registerDataHandler { ev -> receivedEvents.add(ev) }

            // mock event receive
            callback.accept(Mqtt5Publish.builder()
                .topic(topic)
                .payload(event.toByteArray())
                .build())

            // validate callback was executed
            receivedEvents.size shouldBe 1
            receivedEvents[0] shouldBe event

            // unregister handler
            subscriber.removeDataHandler(handlerId)

            // mock event receive
            callback.accept(Mqtt5Publish.builder()
                .topic(topic)
                .payload(event.toByteArray())
                .build())

            // validate callback was not executed
            receivedEvents.size shouldBe 1
        }

    }
}