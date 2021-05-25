package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import io.mockk.*
import org.jetbrains.annotations.NotNull
import org.junit.After
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.type.communication.mqtt.RequestType
import org.openbase.type.communication.mqtt.ResponseType
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.ExecutionException
import java.util.function.BiConsumer
import java.util.function.Consumer

internal class RPCRemoteTest {

    private val client = mockk<Mqtt5AsyncClient>()
    private val topicSlot = slot<String>()
    private val callbackSlot = slot<Consumer<Mqtt5Publish>>()
    private val whenCompleteSlot = slot<BiConsumer<Mqtt5SubAck, Throwable?>>()
    private val requestBytesSlot = slot<ByteArray>()

    private val topic = "/test"
    private val methodName = "add"
    private val args = arrayOf(42, 3)
    private val result = args.sum()
    private val resultAsProtoAny = RPCMethod.anyToProtoAny(result::class.java)(result)
    private val protoAnyToArgs = args.map { arg -> RPCMethod.protoAnyToAny(arg::class.java) }

    @After
    fun clearClientMock() {
        clearAllMocks()
    }

    @Before
    fun initClientMock() {
        println("Before method called!")

        every { client.subscribeWith() } returns
                mockk {
                    every { topicFilter(capture(topicSlot)) } returns //TODO verify structure of topic
                            mockk {
                                every { qos(MqttQos.EXACTLY_ONCE) } answers { self as @NotNull Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start.Complete }
                                every { callback(capture(callbackSlot)) } returns //TODO validate callback
                                        mockk {
                                            every { executor(GlobalCachedExecutorService.getInstance().executorService) } answers { self as @NotNull Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Call.Ex }
                                            every { send() } returns
                                                    mockk {
                                                        every { whenComplete(capture(whenCompleteSlot)) } returns null
                                                    }
                                        }
                            }
                }

        every { client.publishWith() } returns
                mockk {
                    every { topic("$topic/rpc") } returns
                            mockk {
                                every { qos(MqttQos.EXACTLY_ONCE) } answers { self as @NotNull Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>> }
                                every { payload(capture(requestBytesSlot)) } answers { self as @NotNull Mqtt5PublishBuilder.Send.Complete<CompletableFuture<Mqtt5PublishResult>> }
                                every { send() } returns CompletableFuture()
                            }
                }
    }

    private fun mockUnsubscribe(topic: String) {
        every { client.unsubscribeWith() } returns
                mockk {
                    every { topicFilter(topic) } returns
                            mockk {
                                every { send() } returns CompletableFuture()
                            }
                }
    }

    @Test
    fun `test successfull method call`() {
        // create remote and make call
        val rpcRemote = RPCRemote(client, topic)
        val rpcFuture = rpcRemote.callMethod(methodName, Int::class.java, *args)
        Assert.assertFalse("RPCFuture should not be done without sending request", rpcFuture.isDone)

        // validate the subscription for the response
        val requestId = topicSlot.captured.split("/").last()
        UUID.fromString(requestId) // validates that request id is a valid UUID
        Assert.assertEquals(
            "Unexpected structure of response subscription topic",
            "$topic/rpc/$requestId",
            topicSlot.captured
        )

        // simulate broker response that subscription worked
        whenCompleteSlot.captured.accept(mockk(), null)
        Assert.assertFalse("RPCFuture should not be done without receiving response", rpcFuture.isDone)

        // validate the request send to the RPC Server
        mockUnsubscribe(topicSlot.captured)
        val request = RequestType.Request.parseFrom(requestBytesSlot.captured)
        Assert.assertEquals("Request type id differs from listening topic", requestId, request.id)
        Assert.assertEquals("Request type contains wrong method name", methodName, request.methodName)
        Assert.assertEquals("Request contains wrong number of parameters", args.size, request.paramsCount)
        val requestArgs = request.paramsList
            .zip(protoAnyToArgs)
            .map { (arg, protoAnyToArg) -> protoAnyToArg(arg) }
        Assert.assertEquals("Request contains unexpected parameters", args.toList(), requestArgs)

        // validate behaviour to different responses from the RPC Server
        val ackResponse = ResponseType.Response.newBuilder()
            .setStatus(ResponseType.Response.Status.ACKNOWLEDGED)
            .build()
            .toByteArray()
        val progressingResponse = ResponseType.Response.newBuilder()
            .setStatus(ResponseType.Response.Status.PROGRESSING)
            .build()
            .toByteArray()
        val finishedResponse = ResponseType.Response.newBuilder()
            .setStatus(ResponseType.Response.Status.FINISHED)
            .setResult(resultAsProtoAny)
            .build()
            .toByteArray()
        val responseMock = mockk<Mqtt5Publish>()
        every { responseMock.payloadAsBytes } returns ackResponse andThen progressingResponse andThen finishedResponse
        callbackSlot.captured.accept(responseMock)
        Assert.assertFalse("RPCFuture should not be done after receiving acknowledged status", rpcFuture.isDone)

        callbackSlot.captured.accept(responseMock)
        Assert.assertFalse("RPCFuture should not be done after receiving progressing status", rpcFuture.isDone)

        callbackSlot.captured.accept(responseMock)
        Assert.assertTrue("RPCFuture is not done after receiving finished status", rpcFuture.isDone)
        Assert.assertEquals("RPCFuture returned wrong result", result, rpcFuture.get())

        verify(exactly = 1) { client.subscribeWith() }
        verify(exactly = 1) { client.publishWith() }
        verify(exactly = 1) { client.unsubscribeWith() }
    }

    @Test
    fun `test server error`() {
        // create remote and make call
        val rpcRemote = RPCRemote(client, topic)
        val rpcFuture = rpcRemote.callMethod(methodName, Int::class.java, *args)
        Assert.assertFalse("RPCFuture should not be done without sending request", rpcFuture.isDone)

        // simulate broker response that subscription worked
        whenCompleteSlot.captured.accept(mockk(), null)
        Assert.assertFalse("RPCFuture should not be done without receiving response", rpcFuture.isDone)

        // validate server error correctly passed through
        mockUnsubscribe(topicSlot.captured)
        val errorText = "example error"
        val responseMock = mockk<Mqtt5Publish>()
        every { responseMock.payloadAsBytes } returns ResponseType.Response.newBuilder()
            .setStatus(ResponseType.Response.Status.FINISHED)
            .setError(errorText)
            .build()
            .toByteArray()
        callbackSlot.captured.accept(responseMock)
        Assert.assertTrue("RPCFuture is not done after receiving finished status", rpcFuture.isDone)
        Assert.assertThrows(ExecutionException::class.java) { rpcFuture.get() }
        try {
            rpcFuture.get()
        } catch (ex: ExecutionException) {
            Assert.assertTrue("Cause should be a CouldNotPerformException", ex.cause is CouldNotPerformException)
            Assert.assertEquals("Errormessage was not passed through", errorText, ex.cause?.message)
        }

        verify(exactly = 1) { client.subscribeWith() }
        verify(exactly = 1) { client.publishWith() }
        verify(exactly = 1) { client.unsubscribeWith() }
    }

    @Test
    fun `test subscription error`() {
        val rpcRemote = RPCRemote(client, topic)
        var rpcFuture = rpcRemote.callMethod(methodName, Int::class.java, *args)
        Assert.assertFalse("RPCFuture should not be done without sending request", rpcFuture.isDone)

        val exception = Exception("Subscription failed")
        whenCompleteSlot.captured.accept(mockk(), exception)
        Assert.assertTrue("Remote call future is not done after subscription failed", rpcFuture.isDone)
        Assert.assertThrows(ExecutionException::class.java) { rpcFuture.get() }
        try {
            rpcFuture.get()
        } catch (ex: ExecutionException) {
            Assert.assertEquals("", ex.cause, exception)
        }

        verify(exactly = 1) { client.subscribeWith() }
        verify(exactly = 0) { client.publishWith() }
        verify(exactly = 0) { client.unsubscribeWith() }
    }
}
