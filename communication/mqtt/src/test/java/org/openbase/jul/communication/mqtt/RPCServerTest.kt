package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import io.kotest.matchers.shouldBe
import io.mockk.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestInstance
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType
import org.openbase.type.communication.mqtt.ResponseType
import java.util.concurrent.CompletableFuture
import java.util.function.Consumer

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
internal class RPCServerTest {

    private val baseTopic = "/test/server"
    private val rpcServer: RPCServer

    private val mqttClient: Mqtt5AsyncClient = mockk()
    private val mqttSubscribeSlot = slot<Mqtt5Subscribe>()

    val mqttPublishSlot = slot<Mqtt5Publish>()
    val callbackSlot = slot<Consumer<Mqtt5Publish>>()

    init {
        rpcServer = RPCServer(mqttClient, baseTopic)
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
        } returns CompletableFuture()
        every { mqttClient.unsubscribe(any()) } returns CompletableFuture()
        every { mqttClient.publish(capture(mqttPublishSlot)) } returns CompletableFuture()
    }

    @Test
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
    }

    @Test
    fun `test deactivation`() {
        val expectedMqttUnsubscribe = Mqtt5Unsubscribe.builder()
            .topicFilter("$baseTopic/rpc")
            .build()

        rpcServer.deactivate()

        verify(exactly = 1) {
            mqttClient.unsubscribe(expectedMqttUnsubscribe)
        }
    }

    /*@Test
    fun addMethod() {
        rpcServer.addMethod(Any, )
    }*/

    @Nested
    inner class TestRequestHandling {
        //TODO: the tests in this nested class could be improved, e.g. by mocking the rpcMethod and its
        // invokation

        inner class Adder {
            fun add(a: Int, b: Int): Int {
                if (a > 10) {
                    throw CouldNotPerformException("Number too high")
                }

                return a + b
            }
        }

        private val adder = Adder()
        private val requestId = "00000000-0000-0000-0000-000000000001"
        private var mqtt5Publish = mockk<Mqtt5Publish>()
        private var request = RequestType.Request.newBuilder()
            .setId(requestId)
        private var acknowledgedResponse = ResponseType.Response.newBuilder()
            .setId(requestId)
            .setStatus(ResponseType.Response.Status.ACKNOWLEDGED)
            .build()

        lateinit var callback: Consumer<Mqtt5Publish>

        init {
            val method = Adder::class.java.getMethod("add", Int::class.java, Int::class.java)
            rpcServer.addMethod(method, adder)

            every { mqtt5Publish.payloadAsBytes } answers { request.build().toByteArray() }
        }

        @BeforeEach
        fun setupSubscriptionCallback() {
            rpcServer.activate()
            callback = callbackSlot.captured
        }

        @Test
        fun `test bad request id`() {
            //TODO: verify that id is a valid uuid
        }

        @Test
        fun `test unknown method request`() {
            request.methodName = "UnavailableMethod"

            callback.accept(mqtt5Publish)

            val expectedErrorResponse = ResponseType.Response.newBuilder()
                .setId(requestId)
                .setStatus(ResponseType.Response.Status.FINISHED)
                .setError("Method ${request.methodName} is not available")
                .build()

            verifyOrder {
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(acknowledgedResponse.toByteArray())
                    .build())
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(expectedErrorResponse.toByteArray())
                    .build())
            }
        }

        @Test
        fun `test error in invoked method`() {
            request.methodName = "add"
            val args = arrayOf(12, 5)
            val argsAsProtoAny = args
                .map { arg -> RPCMethod.anyToProtoAny(arg::class.java) }
                .zip(args)
                .map { (toProtoAny, arg) -> toProtoAny(arg) }

            request.addAllParams(argsAsProtoAny)
            callback.accept(mqtt5Publish)

            val expectedErrorResponse = ResponseType.Response.newBuilder()
                .setId(requestId)
                .setStatus(ResponseType.Response.Status.FINISHED)
                .setError("Number too high")
                .build()

            verifyOrder {
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(acknowledgedResponse.toByteArray())
                    .build())
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(expectedErrorResponse.toByteArray())
                    .build())
            }
        }

        @Test
        fun `test erroneous parameter count`() {
            request.methodName = "add"
            val args = arrayOf(12, 5, 6)
            val argsAsProtoAny = args
                .map { arg -> RPCMethod.anyToProtoAny(arg::class.java) }
                .zip(args)
                .map { (toProtoAny, arg) -> toProtoAny(arg) }

            request.addAllParams(argsAsProtoAny)
            callback.accept(mqtt5Publish)

            val expectedErrorResponse = ResponseType.Response.newBuilder()
                .setId(requestId)
                .setStatus(ResponseType.Response.Status.FINISHED)
                .setError("Invalid number of arguments! Expected 2 but got ${args.size}")
                .build()

            verifyOrder {
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(acknowledgedResponse.toByteArray())
                    .build())
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(expectedErrorResponse.toByteArray())
                    .build())
            }
        }

        @Test
        fun `test successful method request`() {
            request.methodName = "add"
            val args = arrayOf(6, 5)
            val argsAsProtoAny = args
                .map { arg -> RPCMethod.anyToProtoAny(arg::class.java) }
                .zip(args)
                .map { (toProtoAny, arg) -> toProtoAny(arg) }

            request.addAllParams(argsAsProtoAny)
            callback.accept(mqtt5Publish)

            val expectedResult = adder.add(args[0], args[1])
            val expectedResultProto = RPCMethod.anyToProtoAny(Int::class.java)(expectedResult)
            val expectedResponse = ResponseType.Response.newBuilder()
                .setId(requestId)
                .setStatus(ResponseType.Response.Status.FINISHED)
                .setResult(expectedResultProto)
                .build()

            verifyOrder {
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(acknowledgedResponse.toByteArray())
                    .build())
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(expectedResponse.toByteArray())
                    .build())
            }
        }
    }
}