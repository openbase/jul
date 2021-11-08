package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.MqttGlobalPublishFilter
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5BlockingClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5ClientConfig
import com.hivemq.client.mqtt.mqtt5.Mqtt5RxClient
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5Connect
import com.hivemq.client.mqtt.mqtt5.message.connect.Mqtt5ConnectBuilder
import com.hivemq.client.mqtt.mqtt5.message.connect.connack.Mqtt5ConnAck
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5Disconnect
import com.hivemq.client.mqtt.mqtt5.message.disconnect.Mqtt5DisconnectBuilder
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishResult
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.subscribe.suback.Mqtt5SubAck
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5UnsubscribeBuilder
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.unsuback.Mqtt5UnsubAck
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.exception.NotAvailableException
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
    fun get(communicatorConfig: CommunicatorConfig): Mqtt5AsyncClient {
        if (!sharedClients.containsKey(communicatorConfig)) {
            val client = MqttClient.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(communicatorConfig.hostname)
                .serverPort(communicatorConfig.port)
                .useMqttVersion5()
                .buildAsync()
            val wrappedClient = Mqtt5ClientWrapper(client)
            wrappedClient.connect()
            sharedClients[communicatorConfig] = wrappedClient;
        }

        return sharedClients[communicatorConfig]!!
    }

    fun waitForShutdown() {
        sharedClients.values.forEach { client -> client.disconnect().get() }
    }

    override fun shutdown() {
        sharedClients.values.forEach { client -> client.disconnect() }
    }

    /**
     * Wrapper around an MQTT client to enable sharing.
     * It tracks the amount of subscriptions to a topic and only unsubscribes if
     * unsubscribe is called on the last client.
     */
    internal class Mqtt5ClientWrapper(private val internalClient: Mqtt5AsyncClient) : Mqtt5AsyncClient {

        /**
         * Map topics to the number of times subscribed to.
         */
        private val subscriptionsCounterMap: MutableMap<String, Int> = mutableMapOf()

        private fun increaseTopicCounter(topic: String) {
            subscriptionsCounterMap[topic] = subscriptionsCounterMap.getOrPut(topic) { 0 } + 1
        }

        private fun decreaseTopicCounter(topic: String): Boolean {
            if (subscriptionsCounterMap[topic] == null || subscriptionsCounterMap[topic] == 0) {
                throw NotAvailableException("No subscription to topic $topic")
            }
            subscriptionsCounterMap[topic] = subscriptionsCounterMap[topic]!! - 1
            return subscriptionsCounterMap[topic] == 0
        }

        override fun getConfig(): Mqtt5ClientConfig {
            return internalClient.config
        }

        override fun toRx(): Mqtt5RxClient {
            return internalClient.toRx()
        }

        override fun toBlocking(): Mqtt5BlockingClient {
            return internalClient.toBlocking()
        }

        override fun connect(): CompletableFuture<Mqtt5ConnAck> {
            return internalClient.connect()
        }

        override fun connect(p0: Mqtt5Connect): CompletableFuture<Mqtt5ConnAck> {
            return internalClient.connect(p0)
        }

        override fun connectWith(): Mqtt5ConnectBuilder.Send<CompletableFuture<Mqtt5ConnAck>> {
            return internalClient.connectWith()
        }

        override fun subscribe(p0: Mqtt5Subscribe): CompletableFuture<Mqtt5SubAck> {
            increaseTopicCounter(p0.subscriptions[0].topicFilter.toString())
            return internalClient.subscribe(p0)
        }

        override fun subscribe(p0: Mqtt5Subscribe, p1: Consumer<Mqtt5Publish>): CompletableFuture<Mqtt5SubAck> {
            increaseTopicCounter(p0.subscriptions[0].topicFilter.toString())
            return internalClient.subscribe(p0, p1)
        }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
            p2: Executor
        ): CompletableFuture<Mqtt5SubAck> {
            increaseTopicCounter(p0.subscriptions[0].topicFilter.toString())
            return internalClient.subscribe(p0, p1, p2)
        }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
            p2: Boolean
        ): CompletableFuture<Mqtt5SubAck> {
            increaseTopicCounter(p0.subscriptions[0].topicFilter.toString())
            return internalClient.subscribe(p0, p1, p2)
        }

        override fun subscribe(
            p0: Mqtt5Subscribe,
            p1: Consumer<Mqtt5Publish>,
            p2: Executor,
            p3: Boolean
        ): CompletableFuture<Mqtt5SubAck> {
            increaseTopicCounter(p0.subscriptions[0].topicFilter.toString())
            return internalClient.subscribe(p0, p1, p2, p3)
        }

        override fun subscribeWith(): Mqtt5AsyncClient.Mqtt5SubscribeAndCallbackBuilder.Start {
            TODO("Not yet implemented")
        }

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>) {
            TODO("Not yet implemented")
        }

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>, p2: Executor) {
            TODO("Not yet implemented")
        }

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>, p2: Boolean) {
            TODO("Not yet implemented")
        }

        override fun publishes(p0: MqttGlobalPublishFilter, p1: Consumer<Mqtt5Publish>, p2: Executor, p3: Boolean) {
            TODO("Not yet implemented")
        }

        override fun unsubscribe(p0: Mqtt5Unsubscribe): CompletableFuture<Mqtt5UnsubAck> {
            if (decreaseTopicCounter(p0.topicFilters[0].toString())) {
                return internalClient.unsubscribe(p0)
            }

            val future = CompletableFuture<Mqtt5UnsubAck>()
            future.complete(null)
            return future
        }

        override fun unsubscribeWith(): Mqtt5UnsubscribeBuilder.Send.Start<CompletableFuture<Mqtt5UnsubAck>> {
            TODO("Not yet implemented")
        }

        override fun publish(p0: Mqtt5Publish): CompletableFuture<Mqtt5PublishResult> {
            return internalClient.publish(p0)
        }

        override fun publishWith(): Mqtt5PublishBuilder.Send<CompletableFuture<Mqtt5PublishResult>> {
            TODO("Not yet implemented")
        }

        override fun reauth(): CompletableFuture<Void> {
            return internalClient.reauth()
        }

        override fun disconnect(): CompletableFuture<Void> {
            return internalClient.disconnect()
        }

        override fun disconnect(p0: Mqtt5Disconnect): CompletableFuture<Void> {
            return internalClient.disconnect(p0)
        }

        override fun disconnectWith(): Mqtt5DisconnectBuilder.Send<CompletableFuture<Void>> {
            TODO("Not yet implemented")
        }

    }
}