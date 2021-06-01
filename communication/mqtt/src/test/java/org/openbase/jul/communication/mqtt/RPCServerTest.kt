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

        /*val callbackSlot = slot<Consumer<Mqtt5Publish>>()

        var mqttClient = mockk<Mqtt5AsyncClient>()
        every { mqttClient.subscribeWith() } returns
                mockk {
                    every { topicFilter("$topic/rpc") } returns
                            mockk {
                                every { qos(MqttQos.EXACTLY_ONCE) } answers {self as @NotNull Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start.Complete}
                                every { callback(capture(callbackSlot)) } returns
                                        mockk {
                                            every { executor(GlobalCachedExecutorService.getInstance().executorService) } answers {self as @NotNull Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Call.Ex}
                                            every { send() } returns CompletableFuture()
                                        }
                            }
                }

        val rpcServer = RPCServer(mqttClient, topic)
        rpcServer.activate()

        verify (exactly = 1) {
            mqttClient.subscribeWith()
        }

        val args = arrayOf(34, 5)
        val argsAsProtoAny = args
            .map { arg -> RPCMethod.anyToProtoAny(arg::class.java) }
            .zip(args)
            .map { (toProtoAny, arg) -> toProtoAny(arg) }
        val methodReturn = args.sum()

        val instance = Any()
        val methodMock = mockk<Method>()
        every { methodMock.name } returns "add"
        every { methodMock.returnType } returns Int::class.java
        every { methodMock.parameterTypes } returns arrayOf(Int::class.java, Int::class.java)
        every { methodMock.invoke(instance, args[0], args[1]) } returns methodReturn
        rpcServer.addMethod(methodMock, instance)

        val request = RequestType.Request.newBuilder()
            .setId(UUID.randomUUID().toString())
            .setMethodName(methodMock.name)
            .addAllParams(argsAsProtoAny)
            .build()
        val requestMock = mockk<Mqtt5Publish>()
        every { requestMock.payloadAsBytes } returns request.toByteArray()

        val payloadSlot = slot<ByteArray>()
        every { mqttClient.publishWith() } returns
                mockk {
                    every { topic("$topic/rpc/${request.id}") } returns
                            mockk {
                                every { qos(MqttQos.EXACTLY_ONCE) } answers {self as @NotNull Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>>}
                                every { payload(capture(payloadSlot)) } answers {self as @NotNull Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>>}
                                every { send() } returns CompletableFuture()
                            }
                }

        callbackSlot.captured.accept(requestMock)

        val response = ResponseType.Response.parseFrom(payloadSlot.captured)
        Assert.assertEquals("RPC finished response with unexpected status", ResponseType.Response.Status.FINISHED, response.status)
        Assert.assertEquals("RPC call returned with unexpected result", methodReturn, response.result.unpack(PrimitiveType.Primitive::class.java).int)

        verify (atLeast = 1) {
            mqttClient.publishWith()
        }*/
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

            val ackResponse = ResponseType.Response.newBuilder()
                .setId(requestId)
                .setStatus(ResponseType.Response.Status.ACKNOWLEDGED)
                .build()
            val expectedErrorResponse = ResponseType.Response.newBuilder()
                .setId(requestId)
                .setStatus(ResponseType.Response.Status.FINISHED)
                .setError("Method ${request.methodName} is not available")
                .build()

            verifyOrder {
                mqttClient.publish(Mqtt5Publish.builder()
                    .topic("$baseTopic/rpc/$requestId")
                    .qos(MqttQos.EXACTLY_ONCE)
                    .payload(ackResponse.toByteArray())
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

        }
    }
}