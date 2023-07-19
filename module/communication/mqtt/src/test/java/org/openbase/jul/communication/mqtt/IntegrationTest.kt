package org.openbase.jul.communication.mqtt

import com.google.protobuf.Any
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.fail
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.exception.RPCResolvedException
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType
import org.openbase.type.communication.mqtt.PrimitiveType
import java.util.concurrent.ExecutionException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantLock
import kotlin.concurrent.withLock

class IntegrationTest : AbstractIntegrationTest() {

    private val scope = ScopeProcessor.generateScope("/test/integration")
    private val config = CommunicatorConfig(brokerHost, brokerPort)

    internal class MethodMock {
        val lock = ReentrantLock()
        val errorMessage = "I cannot do this!"

        fun add(a: Int, b: Int) = a + b

        fun couldNotPerform(): Nothing = throw CouldNotPerformException(errorMessage)

        fun ping(time: Long) = time

        fun blockingRPC(msg: String) = lock.withLock { msg }
    }

    @Test
    fun `test exception resolving`() {
        val instance = MethodMock()
        val scope = ScopeProcessor.concat(scope, ScopeProcessor.generateScope("error_handling"))

        val rpcServer = RPCServerImpl(scope, config)
        rpcServer.registerMethod(MethodMock::couldNotPerform, instance)
        rpcServer.activate()
        rpcServer.getActivationFuture()!!.get()

        val rpcClient = RPCClientImpl(scope, config)
        shouldThrow<ExecutionException> {
            rpcClient.callMethod(instance::couldNotPerform.name, Unit::class).get(1, TimeUnit.SECONDS)
        }
        try {
            rpcClient.callMethod(instance::couldNotPerform.name, Unit::class).get(1, TimeUnit.SECONDS)
        } catch (ex: ExecutionException) {
            ex.cause shouldNotBe null
            ex.cause?.let { cause ->
                cause::class.java shouldBe RPCResolvedException::class.java
                val rpcException = cause as RPCResolvedException

                rpcException.cause shouldNotBe null
                rpcException.cause?.let { rpcExceptionCause ->
                    rpcExceptionCause::class.java shouldBe CouldNotPerformException::class.java
                    val initialCause: CouldNotPerformException = rpcExceptionCause as CouldNotPerformException
                    initialCause.message shouldBe instance.errorMessage
                }
            }
        }
    }

    @Test
    @Timeout(value = 30)
    fun `test rpc over mqtt`() {
        val instance = MethodMock()

        val rpcServer = RPCServerImpl(scope, config)
        rpcServer.registerMethod(MethodMock::add, instance)
        rpcServer.activate()
        rpcServer.getActivationFuture()!!.get()

        val rpcClient = RPCClientImpl(scope, config)

        val expectedResult = 45
        val result = rpcClient.callMethod(instance::add.name, Int::class, 3, 42).get(1, TimeUnit.SECONDS)

        result.response shouldBe expectedResult
    }

    @Test
    @Timeout(value = 30)
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

    @Timeout(5)
    @Test
    fun `test parallel method call`() {
        val instance = MethodMock()
        val scope = ScopeProcessor.concat(scope, ScopeProcessor.generateScope("parallel_execution"))

        val rpcServer = RPCServerImpl(scope, config)
        rpcServer.registerMethod(MethodMock::blockingRPC, instance)
        rpcServer.registerMethod(MethodMock::ping, instance)
        rpcServer.activate()
        rpcServer.getActivationFuture()!!.get()

        val rpcClient = RPCClientImpl(scope, config)

        val expectedMsg = "hello"
        val time = 42L
        instance.lock.withLock {
            rpcClient
                .callMethod(instance::blockingRPC.name, String::class, expectedMsg)
                .also { blockingFuture ->
                    try {
                        rpcClient
                            .callMethod(instance::ping.name, Long::class, time)
                            .get(100, TimeUnit.MILLISECONDS)
                            .response shouldBe time
                    } catch (ex: TimeoutException) {
                        fail("Could not ping while another RPCMethod is blocking!", ex)
                    }
                    blockingFuture.isDone shouldBe false
                }
        }.also { blockingFuture ->
            blockingFuture.get(100, TimeUnit.MILLISECONDS).response shouldBe expectedMsg
        }
    }
}
