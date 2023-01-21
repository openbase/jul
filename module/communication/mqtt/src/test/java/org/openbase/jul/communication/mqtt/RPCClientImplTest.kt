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
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.EnumSource
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.data.RPCResponse
import org.openbase.jul.communication.exception.RPCResolvedException
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType.Request
import org.openbase.type.communication.mqtt.ResponseType.Response
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.concurrent.Future
import java.util.function.BiConsumer
import java.util.function.Consumer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RPCClientImplTest {

    private val mqttClient: Mqtt5AsyncClient = mockk()

    init {
        mockkObject(SharedMqttClient)
        every { SharedMqttClient.get(any()) } returns mqttClient
    }

    private val mqttSubscribeSlot = slot<Mqtt5Subscribe>()

    val mqttPublishSlot = slot<Mqtt5Publish>()
    val callbackSlot = slot<Consumer<Mqtt5Publish>>()
    val afterSubscriptionSlot = slot<BiConsumer<Mqtt5SubAck, Throwable?>>()

    private var rpcRemote: RPCClientImpl

    private val baseTopic = "/test/remote"
    private val methodName = "add"
    private val requestId = "00000000-000-0000-0000-000000000001"
    private val args = arrayOf(42, 3)
    private val expectedResult = 42

    init {
        rpcRemote = spyk(
            RPCClientImpl(ScopeProcessor.generateScope(baseTopic), CommunicatorConfig("localhost", 1000)),
            recordPrivateCalls = true
        )
        every { rpcRemote.generateRequestId() } returns requestId
    }

    @AfterAll
    fun clearMocks() {
        clearAllMocks()
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
    @Timeout(value = 30)
    fun `test method call subscription`() {
        val expectedMqttSubscribe = Mqtt5Subscribe.builder()
            .topicFilter("$baseTopic/rpc/$requestId")
            .qos(MqttQos.EXACTLY_ONCE)
            .build()

        val rpcFuture = rpcRemote.callMethod(methodName, expectedResult::class, *args)
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

        private lateinit var rpcFuture: Future<out RPCResponse<out Int>>
        private lateinit var callback: BiConsumer<Mqtt5SubAck, Throwable?>

        @BeforeEach
        fun setupMethodCall() {
            rpcFuture = rpcRemote.callMethod(methodName, expectedResult::class, *args)
            callback = afterSubscriptionSlot.captured
        }

        @Test
        @Timeout(value = 30)
        fun `test subscription failed`() {
            val expectedException = CouldNotPerformException("Could not subscribe to topic")

            callback.accept(mockk(), expectedException)

            val exception = shouldThrow<ExecutionException> { rpcFuture.get() }
            exception.cause shouldBe expectedException
        }

        @Test
        @Timeout(value = 30)
        fun `test successful subscription`() {
            val expectedRequest: Request = Request.newBuilder()
                .setId(requestId)
                .setMethodName(methodName)
                .addAllParams(args
                    .zip(args
                        .map { arg -> RPCMethodWrapper.anyToProtoAny(arg::class) })
                    .map { (arg, toProtoAny) -> toProtoAny(arg) })
                .build()
            val expectedMqttPublish = Mqtt5Publish.builder()
                .topic("$baseTopic/rpc")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(expectedRequest.toByteArray())
                .build()

            callback.accept(mockk(), null)

            rpcFuture.isDone shouldNotBe true
            mqttPublishSlot.captured.clearTimestamp() shouldBe expectedMqttPublish
        }
    }

    @Nested
    inner class TestRPCResponseCallback {

        private var mqtt5Publish = mockk<Mqtt5Publish>(relaxed = true)
        private var response = Response.newBuilder()

        private lateinit var rpcFuture: Future<out RPCResponse<out Int>>
        private lateinit var callback: Consumer<Mqtt5Publish>

        init {
            every { mqtt5Publish.payloadAsBytes } answers { response.build().toByteArray() }
        }

        @BeforeEach
        fun setupMethodCall() {
            rpcFuture = rpcRemote.callMethod(methodName, expectedResult::class, *args)
            callback = callbackSlot.captured
        }

        @Test
        @Timeout(value = 30)
        fun `test error response`() {
            val errorMessage = "RPCServer answered with an error"
            response.error = CouldNotPerformException(errorMessage).stackTraceToString()

            callback.accept(mqtt5Publish)

            val exception = shouldThrow<ExecutionException> { rpcFuture.get() }
            exception.cause shouldNotBe null
            exception.cause?.let { cause ->
                cause::class.java shouldBe RPCResolvedException::class.java

                cause.cause shouldNotBe null
                cause.cause?.let { initialCause ->
                    initialCause::class.java shouldBe CouldNotPerformException::class.java
                    initialCause.message shouldBe errorMessage
                }
            }

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
        @Timeout(value = 30)
        fun `test successful response`() {
            response.status = Response.Status.FINISHED
            response.result = RPCMethodWrapper.anyToProtoAny(expectedResult::class)(expectedResult)

            callback.accept(mqtt5Publish)

            rpcFuture.isDone shouldBe true
            rpcFuture.get().response shouldBe expectedResult

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
