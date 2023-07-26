package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.datatypes.MqttTopicFilter
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.iface.Shutdownable
import java.util.*
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.function.Consumer

// TODO:
//  * Implement sharing correctly:
//    * Count instances accessing and disconnect if none is anymore
//    * save connecting future and make accessible
object SharedMqttClient : Shutdownable {

    private var sharedClients: MutableMap<CommunicatorConfig, Mqtt5AsyncClient> = mutableMapOf()

    init {
        Shutdownable.registerShutdownHook(this)
    }

    @Synchronized
    fun get(
        communicatorConfig: CommunicatorConfig,
    ) = sharedClients.getOrPut(communicatorConfig) {
        MqttClient.builder()
            .identifier(UUID.randomUUID().toString())
            .serverHost(communicatorConfig.hostname)
            .serverPort(communicatorConfig.port)
            .useMqttVersion5()
            .automaticReconnectWithDefaultConfig()
            .buildAsync()
            .let { Mqtt5ClientWrapper(it) }
            .also { it.connect() }
    }

    @Synchronized
    fun waitForShutdown() =
        sharedClients.values
            .filter { (it as Mqtt5ClientWrapper).isConnected() }
            .map { it.disconnect() }
            .map { it.get() }
            .run { sharedClients.clear() }

    @Synchronized
    override fun shutdown(): Unit =
        sharedClients.values
            .filter { (it as Mqtt5ClientWrapper).isConnected() }
            .onEach { it.disconnect() }
            .run { sharedClients.clear() }

    /**
     * Wrapper around an MQTT client to enable sharing.
     * It tracks the amount of subscriptions to a topic and only unsubscribes if
     * unsubscribe is called on the last client.
     */
    internal class Mqtt5ClientWrapper(
        private val internalClient: Mqtt5AsyncClient,
    ) : Mqtt5AsyncClient {

        /**
         * Map topics to the number of times subscribed to.
         */
        private val subscriptionsCounterMap: MutableMap<String, Int> = mutableMapOf()

        fun isConnected() = internalClient.config.state.isConnected

        /**
         * Increase the counter for the number of subscriptions on a topic.
         *
         * @param topic the topic
         * @return if this is the first subscription to this topic
         */
        @Synchronized
        private fun increaseTopicCounter(topic: MqttTopicFilter): Boolean = topic
            .toString()
            .also { subscriptionsCounterMap[it] = subscriptionsCounterMap.getOrPut(it) { 0 } + 1 }
            .let { subscriptionsCounterMap[it] == 1 }

        /**
         * Decrease the counter for the number of subscriptions on a topic.
         *
         * @param topicFilter the topic
         * @return true if there are still subscriptions on this topic after decreasing the counter
         */
        @Synchronized
        private fun decreaseTopicCounter(topicFilter: MqttTopicFilter): Boolean = topicFilter
            .toString()
            .let { topic ->
                subscriptionsCounterMap[topic]
                    .let { it == null || it == 0 }
                    .also { if (it) return true }

                subscriptionsCounterMap[topic] = subscriptionsCounterMap[topic]!!.dec()
                (subscriptionsCounterMap[topic] == 0)
                    .also { if (it) subscriptionsCounterMap.remove(topic) }
            }

        override fun getConfig() = internalClient.config

        override fun toRx(): Mqtt5RxClient = internalClient.toRx()

        override fun toBlocking(): Mqtt5BlockingClient = internalClient.toBlocking()

        override fun connect() = internalClient.connect()

        override fun connect(p0: Mqtt5Connect) = internalClient.connect(p0)

        override fun connectWith() = internalClient.connectWith()

        override fun subscribe(
            p0: Mqtt5Subscribe,
        ) = p0.subscriptions
            .map { increaseTopicCounter(it.topicFilter) }
            .let { internalClient.subscribe(p0) }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
        ) = p0.subscriptions
            .map { increaseTopicCounter(it.topicFilter) }
            .let { internalClient.subscribe(p0, p1) }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
            p2: Executor,
        ) = p0.subscriptions
            .map { increaseTopicCounter(it.topicFilter) }
            .let { internalClient.subscribe(p0, p1, p2) }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
            p2: Boolean,
        ) = p0.subscriptions
            .map { increaseTopicCounter(it.topicFilter) }
            .let { internalClient.subscribe(p0, p1, p2) }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
            p2: Executor,
            p3: Boolean,
        ) = p0.subscriptions
            .map { increaseTopicCounter(it.topicFilter) }
            .let { internalClient.subscribe(p0, p1, p2, p3) }

        override fun subscribeWith() = throw NotImplementedError("This method is not supported by this implementation.")

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>) =
            internalClient.publishes(p0, p1)

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>, p2: Executor) =
            internalClient.publishes(p0, p1, p2)

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>, p2: Boolean) =
            internalClient.publishes(p0, p1, p2)

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>, p2: Executor, p3: Boolean) =
            internalClient.publishes(p0, p1, p2, p3)

        override fun unsubscribe(
            p0: Mqtt5Unsubscribe,
        ): CompletableFuture<Mqtt5UnsubAck> = p0.topicFilters
            .filter { decreaseTopicCounter(it) }
            .takeIf { it.isNotEmpty() }
            ?.let {
                Mqtt5Unsubscribe.builder().addTopicFilters(it).build()
                    .also { println("Unsubscribe from topic ${it.toString()}") }
            }
            ?.let { internalClient.unsubscribe(it) }
            ?: CompletableFuture.completedFuture(null)

        override fun unsubscribeWith(): Mqtt5UnsubscribeBuilder.Send.Start<CompletableFuture<Mqtt5UnsubAck>> =
            throw NotImplementedError("This method is not supported by this implementation.")

        override fun publish(p0: Mqtt5Publish) = internalClient.publish(p0)

        override fun publishWith() = internalClient.publishWith()

        override fun reauth() = internalClient.reauth()

        override fun disconnect() = internalClient.disconnect()

        override fun disconnect(p0: Mqtt5Disconnect) = internalClient.disconnect(p0)

        override fun disconnectWith() = internalClient.disconnectWith()
    }
}
