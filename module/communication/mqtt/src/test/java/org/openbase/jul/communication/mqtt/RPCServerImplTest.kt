package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import io.kotest.matchers.ints.shouldBeGreaterThan
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeTypeOf
import io.mockk.*
import org.junit.jupiter.api.*
import org.junit.jupiter.api.Test
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.exception.RPCException
import org.openbase.jul.communication.exception.RPCResolvedException
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType.Request
import org.openbase.type.communication.mqtt.ResponseType.Response
import java.lang.reflect.InvocationTargetException
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RPCServerImplTest {

    private val baseTopic = "/test/server"
    private val rpcServer: RPCServerImpl

    // mock the mqtt client so that no actual communication occurs
    private val mqttClient: Mqtt5AsyncClient = mockk()

    /**
     * Capture the callback which is invoked if an
     * RPCClient calls a method to simulate calls.
     */
    val callbackSlot = slot<Consumer<Mqtt5Publish>>()

    /**
     * Capture all responses send by the server.
     */
    val mqttPublishSlot = mutableListOf<Mqtt5Publish>()

    init {
        mockkObject(SharedMqttClient)
        every { SharedMqttClient.get(any()) } returns mqttClient

        rpcServer = RPCServerImpl(ScopeProcessor.generateScope(baseTopic), CommunicatorConfig("localhost", 1234))
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
                any(),
                capture(callbackSlot),
                GlobalCachedExecutorService.getInstance().executorService
            )
        } returns CompletableFuture.completedFuture(null)
        every { mqttClient.unsubscribe(any()) } returns CompletableFuture.completedFuture(null)
        every { mqttClient.publish(capture(mqttPublishSlot)) } returns CompletableFuture.completedFuture(null)
    }

    @Test
    @Timeout(value = 30)
    fun `test activation`() {
        val expectedMqtt5Subscribe = Mqtt5Subscribe.builder()
            .topicFilter("$baseTopic/rpc")
            .qos(MqttQos.EXACTLY_ONCE)
            .build()

        rpcServer.activate()

        verify {
            mqttClient.subscribe(
                expectedMqtt5Subscribe,
                callbackSlot.captured,
                GlobalCachedExecutorService.getInstance().executorService
            )
        }

        // TODO: verify activation is skipped if called multiple times?
    }

    @Test
    @Timeout(value = 30)
    fun `test deactivation`() {
        val expectedMqttUnsubscribe = Mqtt5Unsubscribe.builder()
            .topicFilter("$baseTopic/rpc")
            .build()

        rpcServer.deactivate()

        verify(exactly = 1) {
            mqttClient.unsubscribe(expectedMqttUnsubscribe)
        }
    }

    @Nested
    inner class TestRequestHandling {
        //TODO: the tests in this nested class could be improved, e.g. by mocking the rpcMethod and its
        // invocation

        inner class Adder {
            fun add(a: Int, b: Int): Int {
                if (a > 10) {
                    throw CouldNotPerformException("Number too high")
                }
                return a + b
            }
        }

        private val validArgs = arrayOf(4, 6)
        private val invalidArgs = arrayOf(12, 5)
        private val invalidArgCount = arrayOf(1, 2, 3)

        private val adder = Adder()
        private val requestId = "00000000-0000-0000-0000-000000000001"

        init {
            rpcServer.registerMethod(Adder::add, adder)
        }

        private lateinit var callback: Consumer<Mqtt5Publish>

        @BeforeEach
        fun setupSubscriptionCallback() {
            //println("Test ${mqttPublishSlot.size}")
            //mqttPublishSlot.clear()

            rpcServer.activate()
            callback = callbackSlot.captured
        }

        private fun simulateMethodCall(
            methodName: String,
            id: String = "00000000-0000-0000-0000-000000000001",
            vararg parameter: Any
        ) {
            val argsAsProtoAny = parameter
                .map { arg -> RPCMethod.anyToProtoAny(arg::class) }
                .zip(parameter)
                .map { (toProtoAny, arg) -> toProtoAny(arg) }

            val request = Request.newBuilder()
                .setId(id)
                .setMethodName(methodName)
                .addAllParams(argsAsProtoAny)
                .build()

            val clientRequest = mockk<Mqtt5Publish>()
            every { clientRequest.payloadAsBytes } answers { request.toByteArray() }

            callback.accept(clientRequest)
        }

        @Test
        @Timeout(value = 30)
        fun `test bad request id`() {
            //TODO: verify that id is a valid uuid
        }

        /**
         * Verify that no matter the request, the server
         * always responds with an acknowledgement first.
         */
        @Test
        @Timeout(value = 30)
        fun `test acknowledgement`() {
            val acknowledgedResponse = Response.newBuilder()
                .setId(requestId)
                .setStatus(Response.Status.ACKNOWLEDGED)
                .build()

            simulateMethodCall(methodName = "something")

            mqttPublishSlot.size shouldBeGreaterThan 1
            mqttPublishSlot[0].clearTimestamp() shouldBe Mqtt5Publish.builder()
                .topic("$baseTopic/rpc/$requestId")
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(acknowledgedResponse.toByteArray())
                .build()
        }

        @Test
        @Timeout(value = 30)
        fun `test unknown method request`() {
            val methodName = "UnavailableMethod"
            simulateMethodCall(methodName = methodName)

            val lastPublish = mqttPublishSlot.last()
            lastPublish.topic.toString() shouldBe "$baseTopic/rpc/$requestId"
            lastPublish.qos shouldBe MqttQos.EXACTLY_ONCE

            val actualResponse = Response.parseFrom(lastPublish.payloadAsBytes)
            actualResponse.id shouldBe requestId
            actualResponse.status shouldBe Response.Status.FINISHED
            actualResponse.hasResult() shouldBe false

            val error = RPCResolvedException.resolveRPCException(RPCException(actualResponse.error))
            error shouldBe NotAvailableException("Method $methodName")
        }

        @Test
        @Timeout(value = 30)
        fun `test error in invoked method`() {
            simulateMethodCall(
                methodName = Adder::add.name,
                parameter = invalidArgs
            )

            val lastPublish = mqttPublishSlot.last()
            lastPublish.topic.toString() shouldBe "$baseTopic/rpc/$requestId"
            lastPublish.qos shouldBe MqttQos.EXACTLY_ONCE

            val actualResponse = Response.parseFrom(lastPublish.payloadAsBytes)
            actualResponse.id shouldBe requestId
            actualResponse.status shouldBe Response.Status.FINISHED
            actualResponse.hasResult() shouldBe false

            val error = RPCResolvedException.resolveRPCException(RPCException(actualResponse.error))
            error.shouldBeTypeOf<CouldNotPerformException>()
        }

        @Test
        @Timeout(value = 30)
        fun `test erroneous parameter count`() {
            simulateMethodCall(
                methodName = Adder::add.name,
                parameter = invalidArgCount
            )

            val lastPublish = mqttPublishSlot.last()
            lastPublish.topic.toString() shouldBe "$baseTopic/rpc/$requestId"
            lastPublish.qos shouldBe MqttQos.EXACTLY_ONCE

            val actualResponse = Response.parseFrom(lastPublish.payloadAsBytes)
            actualResponse.id shouldBe requestId
            actualResponse.status shouldBe Response.Status.FINISHED
            actualResponse.hasResult() shouldBe false

            val error = RPCResolvedException.resolveRPCException(RPCException(actualResponse.error))
            error.shouldBeTypeOf<CouldNotPerformException>()
        }

        @Test
        @Timeout(value = 30)
        fun `test successful method request`() {
            simulateMethodCall(
                methodName = Adder::add.name,
                parameter = validArgs
            )

            val lastPublish = mqttPublishSlot.last()
            lastPublish.topic.toString() shouldBe "$baseTopic/rpc/$requestId"
            lastPublish.qos shouldBe MqttQos.EXACTLY_ONCE

            val actualResponse = Response.parseFrom(lastPublish.payloadAsBytes)
            actualResponse.id shouldBe requestId
            actualResponse.status shouldBe Response.Status.FINISHED
            actualResponse.error.isEmpty() shouldBe true

            val expectedResult = adder.add(validArgs[0], validArgs[1])
            val expectedResultProto = RPCMethod.anyToProtoAny(Int::class)(expectedResult)
            actualResponse.result shouldBe expectedResultProto
        }
    }
}
