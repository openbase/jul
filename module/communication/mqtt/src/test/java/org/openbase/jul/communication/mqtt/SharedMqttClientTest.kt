package org.openbase.jul.communication.mqtt

import com.hivemq.client.internal.mqtt.util.MqttChecks.unsubscribe
import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import com.hivemq.client.mqtt.mqtt5.message.subscribe.Mqtt5Subscribe
import com.hivemq.client.mqtt.mqtt5.message.unsubscribe.Mqtt5Unsubscribe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.Timeout

import org.openbase.jul.communication.config.CommunicatorConfig

internal class SharedMqttClientTest : AbstractIntegrationTest() {

    @Test
    @Timeout(value = 30)
    fun `shutdown should be work as expected`() {
        val config = CommunicatorConfig(brokerHost, brokerPort)
        val client = SharedMqttClient
        client.get(config).apply {
            disconnect()
        }
        client.waitForShutdown()
        client.shutdown()
    }

    @Test
    @Timeout(value = 30)
    fun `subscription should be work as expected`() {
        val config = CommunicatorConfig(brokerHost, brokerPort)
        val topic = "/a/b/c"
        val client = SharedMqttClient

        val subscribeTopic = Mqtt5Subscribe.builder()
            .topicFilter(topic)
            .qos(MqttQos.EXACTLY_ONCE)
            .build()

        val unsubscribeTopic = Mqtt5Unsubscribe.builder()
            .topicFilter(topic)
            .build()

        client.get(config).apply {
            subscribe(subscribeTopic)
            subscribe(subscribeTopic)
            unsubscribe(unsubscribeTopic)
            unsubscribe(unsubscribeTopic)
            disconnect()
        }
        client.waitForShutdown()
        client.shutdown()
    }
}
