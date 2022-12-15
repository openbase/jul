package org.openbase.jul.communication.mqtt

import com.hivemq.client.internal.util.AsyncRuntimeException
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import kotlinx.coroutines.*
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.RPCServer
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.NotAvailableException
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.exception.printer.LogLevel
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.jul.schedule.SyncObject
import org.openbase.type.communication.ScopeType.Scope
import org.openbase.type.communication.mqtt.RequestType
import org.openbase.type.communication.mqtt.ResponseType
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.lang.reflect.InvocationTargetException
import java.time.Duration
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import kotlin.reflect.KFunction

class RPCServerImpl(
    scope: Scope,
    config: CommunicatorConfig,
    dispatcher: CoroutineDispatcher? = GlobalCachedExecutorService.getInstance().executorService.asCoroutineDispatcher()
) : RPCCommunicatorImpl(scope, config), RPCServer {

    companion object {
        val NO_DISPATCHER: CoroutineDispatcher? = null
        val RPC_TIMEOUT: Duration = Duration.ofMinutes(3)
    }

    private val logger: Logger = LoggerFactory.getLogger(RPCServerImpl::class.simpleName)

    private val methods: HashMap<String, RPCMethod> = HashMap()
    private var activationFuture: Future<out Any>? = null

    private val lock = SyncObject("Activation Lock")

    private val coroutineScope = if (dispatcher != null) CoroutineScope(dispatcher) else null

    internal fun getActivationFuture(): Future<out Any>? {
        return this.activationFuture
    }

    override fun isActive(): Boolean {
        synchronized(lock) {
            return (activationFuture != null && activationFuture!!.isDone && !activationFuture!!.isCancelled)
        }
    }

    override fun activate() {
        synchronized(lock) {
            if (isActive) {
                return
            }

            activationFuture = mqttClient.subscribe(
                Mqtt5Subscribe.builder()
                    .topicFilter(topic)
                    .qos(MqttQos.EXACTLY_ONCE)
                    .build(),
                { mqtt5Publish ->
                    // Note: this is a wrapper for the usage of a shared client
                    //       which may remain subscribed even if deactivate is called
                    if (isActive) {
                        coroutineScope?.launch {
                            withTimeout(RPC_TIMEOUT.toMillis()) {
                                handleRemoteCall(mqtt5Publish)
                            }
                        } ?: handleRemoteCall(mqtt5Publish)
                    }
                },
                GlobalCachedExecutorService.getInstance().executorService
            )

            try {
                activationFuture!!.get(ACTIVATION_TIMEOUT, TimeUnit.MILLISECONDS)
            } catch (e: TimeoutException) {
                activationFuture!!.cancel(true)
                throw CouldNotPerformException("Could not activate Subscriber", e)
            } catch (e: AsyncRuntimeException) {
                activationFuture!!.cancel(true)
                throw CouldNotPerformException("Could not activate Subscriber", e)
            } catch (e: InterruptedException) {
                activationFuture!!.cancel(true)
                throw e;
            }
        }
    }

    override fun deactivate() {
        synchronized(lock) {
            activationFuture = null
            mqttClient.unsubscribe(
                Mqtt5Unsubscribe.builder()
                    .topicFilter(topic)
                    .build()
            )
        }
        coroutineScope?.cancel()
    }

    override fun registerMethod(method: KFunction<*>, instance: Any) {
        methods[method.name] = RPCMethod(method, instance);
    }

    private fun handleRemoteCall(mqtt5Publish: Mqtt5Publish) {
        val responseBuilder = ResponseType.Response.newBuilder()
        val request = RequestType.Request.parseFrom(mqtt5Publish.payloadAsBytes)

        // make sure that the request id is a valid uuid so that request
        // collisions are unlikely
        responseBuilder.id = UUID.fromString(request.id).toString()
        val requestTopic = topic + "/" + responseBuilder.id
        val mqttResponseBuilder = Mqtt5Publish.builder()
            .topic(requestTopic)
            .qos(MqttQos.EXACTLY_ONCE)

        responseBuilder.status = ResponseType.Response.Status.ACKNOWLEDGED
        responseBuilder.id = request.id
        mqttClient.publish(
            mqttResponseBuilder
                .payload(responseBuilder.build().toByteArray())
                .attachTimestamp()
                .build()
        )

        if (request.methodName !in methods) {
            responseBuilder.status = ResponseType.Response.Status.FINISHED;
            val ex = NotAvailableException("Method ${request.methodName}")
            responseBuilder.error = ex.stackTraceToString()

            mqttClient.publish(
                mqttResponseBuilder
                    .payload(responseBuilder.build().toByteArray())
                    .attachTimestamp()
                    .build()
            )
            return;
        }

        val method = methods[request.methodName]!!;

        //TODO open thread to send PROGRESSING message periodically
        responseBuilder.status = ResponseType.Response.Status.FINISHED
        try {
            val result = method.invoke(request.paramsList)
            responseBuilder.result = result
        } catch (ex: Exception) {
            when (ex) {
                is InvocationTargetException -> responseBuilder.error =
                    ex.cause?.stackTraceToString() ?: ex.stackTraceToString()

                else -> {
                    ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN)
                    responseBuilder.error = CouldNotPerformException("Server error ${ex.message}").stackTraceToString()
                }
            }
        }

        mqttClient.publish(
            mqttResponseBuilder
                .payload(responseBuilder.build().toByteArray())
                .attachTimestamp()
                .build()
        )
    }
}
