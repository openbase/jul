package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType.Request
import org.openbase.type.communication.mqtt.ResponseType.Response
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.function.BiConsumer
import java.util.function.Consumer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RPCRemoteTest {

    private val mqttClient: Mqtt5AsyncClient = mockk()
    private val mqttSubscribeSlot = slot<Mqtt5Subscribe>()

    val mqttPublishSlot = slot<Mqtt5Publish>()
    val callbackSlot = slot<Consumer<Mqtt5Publish>>()
    val afterSubscriptionSlot = slot<BiConsumer<Mqtt5SubAck, Throwable?>>()

    private var rpcRemote: RPCRemote

    private val baseTopic = "/test/remote"
    private val methodName = "add"
    private val requestId = "00000000-000-0000-0000-000000000001"
    private val args = arrayOf(42, 3)
    private val expectedResult = 42

    init {
        rpcRemote = spyk(RPCRemote(mqttClient, baseTopic), recordPrivateCalls = true)
        every { rpcRemote.generateRequestId() } returns requestId
    }

    @BeforeEach
    fun initMqttClientMock() {
        clearMocks(mqttClient)

        every {
            mqttClient.subscribe(
                capture(mqttSubscribeSlot),
                capture(callbackSlot),
                GlobalCachedExecutorService.getInstance().executorService
            )
        } returns
                mockk {
                    every { whenComplete(capture(afterSubscriptionSlot)) } returns CompletableFuture()
                }
        every { mqttClient.unsubscribe(any()) } returns CompletableFuture()
        every { mqttClient.publish(capture(mqttPublishSlot)) } returns CompletableFuture()
    }

    @Test
    fun `test method call subscription`() {
        val expectedMqttSubscribe = Mqtt5Subscribe.builder()
            .topicFilter("$baseTopic/rpc/$requestId")
            .qos(MqttQos.EXACTLY_ONCE)
            .build()

        val rpcFuture = rpcRemote.callMethod(methodName, expectedResult::class.java, *args)
        rpcFuture.isDone shouldNotBe true
        mqttSubscribeSlot.captured shouldBe expectedMqttSubscribe
        verify(exactly = 1) {
            mqttClient.subscribe(
                expectedMqttSubscribe,
                callbackSlot.captured,
                GlobalCachedExecutorService.getInstance().executorService
            )
        }
    }

    @Nested
    inner class TestAfterSubscriptionCallback {

        private lateinit var rpcFuture: Future<out Int>
        private lateinit var callback: BiConsumer<Mqtt5SubAck, Throwable?>

        @BeforeEach
        fun setupMethodCall() {
            rpcFuture = rpcRemote.callMethod(methodName, expectedResult::class.java, *args)
            callback = afterSubscriptionSlot.captured
        }

        @Test
        fun `test subscription failed`() {
            val expectedException = CouldNotPerformException("Could not subscribe to topic")

            callback.accept(mockk(), expectedException)

            val exception = shouldThrow<ExecutionException> { rpcFuture.get() }
            exception.cause shouldBe expectedException
        }

        @Test
        fun `test successful subscription`() {
            val expectedRequest: Request = Request.newBuilder()
                .setId(requestId)
                .setMethodName(methodName)
                .addAllParams(args
                    .zip(args
                        .map { arg -> RPCMethod.anyToProtoAny(arg::class.java) })
                    .map { (arg, toProtoAny) -> toProtoAny(arg) })
                .build()
            val expectedMqttPublish = Mqtt5Publish.builder()
                .topic("$baseTopic/rpc")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(expectedRequest.toByteArray())
                .build()

            callback.accept(mockk(), null)

            rpcFuture.isDone shouldNotBe true
            mqttPublishSlot.captured shouldBe expectedMqttPublish
        }
    }

    @Nested
    inner class TestRPCResponseCallback {

        private var mqtt5Publish = mockk<Mqtt5Publish>()
        private var response = Response.newBuilder()

        private lateinit var rpcFuture: Future<out Int>
        private lateinit var callback: Consumer<Mqtt5Publish>

        init {
            every { mqtt5Publish.payloadAsBytes } answers { response.build().toByteArray() }
        }

        @BeforeEach
        fun setupMethodCall() {
            rpcFuture = rpcRemote.callMethod(methodName, expectedResult::class.java, *args)
            callback = callbackSlot.captured
        }

        @Test
        fun `test error response`() {
            response.error = "RPCServer answered with an error"

            callback.accept(mqtt5Publish)

            val exception = shouldThrow<ExecutionException> { rpcFuture.get() }
            exception.cause!!::class.java shouldBe CouldNotPerformException::class.java
            exception.cause!!.message shouldBe response.error

            verify(exactly = 1) {
                mqttClient.unsubscribe(
                    Mqtt5Unsubscribe.builder()
                        .topicFilter("$baseTopic/rpc/$requestId")
                        .build()
                )
            }
        }

        @ParameterizedTest
        @EnumSource(mode = EnumSource.Mode.EXCLUDE, names = ["FINISHED", "UNRECOGNIZED"])
        fun `test unfinished response`(status: Response.Status) {
            response.status = status

            callback.accept(mqtt5Publish)

            rpcFuture.isDone shouldNotBe true
        }

        @Test
        fun `test successful response`() {
            response.status = Response.Status.FINISHED
            response.result = RPCMethod.anyToProtoAny(expectedResult::class.java)(expectedResult)

            callback.accept(mqtt5Publish)

            rpcFuture.isDone shouldBe true
            rpcFuture.get() shouldBe expectedResult

            verify(exactly = 1) {
                mqttClient.unsubscribe(
                    Mqtt5Unsubscribe.builder()
                        .topicFilter("$baseTopic/rpc/$requestId")
                        .build()
                )
            }
        }
    }
}
