package org.openbase.jul.communication.mqtt

import com.hivemq.client.internal.util.AsyncRuntimeException
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.Subscriber
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.schedule.GlobalCachedExecutorService
import org.openbase.jul.schedule.SyncObject
import org.openbase.type.communication.EventType.Event
import org.openbase.type.communication.ScopeType.Scope
import java.util.*
import java.util.concurrent.Future
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException

class SubscriberImpl(
    scope: Scope, config: CommunicatorConfig,
) : CommunicatorImpl(scope, config), Subscriber {

    private val lock = SyncObject("Activation Lock")

    private var activationFuture: Future<out Any>? = null

    private val callbackMap: MutableMap<UUID, (Event, Map<String, String>) -> Any> = mutableMapOf()

    override fun registerDataHandler(callback: (Event) -> Any): UUID {
        return registerDataHandler { event, _ -> callback(event) }
    }

    override fun registerDataHandler(callback: (Event, Map<String, String>) -> Any): UUID {
        val handlerId = UUID.randomUUID()
        callbackMap[handlerId] = callback
        return handlerId
    }

    override fun removeDataHandler(handlerId: UUID) {
        callbackMap.remove(handlerId)
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
                { mqtt5Publish: Mqtt5Publish ->
                    // Note: this is a wrapper for the usage of a shared client
                    //       which may remain subscribed even if deactivate is called
                    if (isActive) {
                        mqtt5Publish.userProperties
                        val event = Event.parseFrom(mqtt5Publish.payloadAsBytes)
                        callbackMap.values.forEach { function ->
                            function(
                                event,
                                mqtt5Publish.userProperties.asList()
                                    .associate { it.name.toString() to it.value.toString() })
                        }
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
    }

    override fun isActive(): Boolean {
        synchronized(lock) {
            return (activationFuture != null && activationFuture!!.isDone && !activationFuture!!.isCancelled)
        }
    }

    internal fun getActivationFuture(): Future<out Any>? {
        return this.activationFuture
    }
}
