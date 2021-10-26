package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.Publisher
import org.openbase.type.communication.EventType
import org.openbase.type.communication.ScopeType

class PublisherImpl( override val scope: ScopeType.Scope,
                     override val config: CommunicatorConfig) : CommunicatorImpl(scope, config), Publisher {
    override fun publish(event: EventType.Event): EventType.Event {
        mqttClient.publish(
            Mqtt5Publish.builder()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(event.toByteArray())
                .build()
        )
        return event
    }

    override fun activate() {
        // see is active
    }

    override fun deactivate() {
        // see is active
    }

    override fun isActive(): Boolean {
        //TODO: active as long as the mqtt client is active which is shared
        return true
    }

}