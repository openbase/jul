package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.datatypes.MqttQos
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.Publisher
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.EventType
import org.openbase.type.communication.ScopeType
import org.openbase.type.communication.ScopeType.Scope

class PublisherImpl(scope: ScopeType.Scope, config: CommunicatorConfig) : CommunicatorImpl(scope, config), Publisher {

    var active = false

    override fun publish(event: EventType.Event, attachTimestamp: Boolean): EventType.Event {
        mqttClient.publish(
            Mqtt5Publish.builder()
                .topic(topic)
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(event.toByteArray())
                .attachTimestamp(attachTimestamp)
                .build()
        )
        return event
    }

    override fun publish(event: EventType.Event, scope: Scope, attachTimestamp: Boolean): EventType.Event {
        mqttClient.publish(
            Mqtt5Publish.builder()
                .topic(ScopeProcessor.generateStringRep(scope))
                .qos(MqttQos.EXACTLY_ONCE)
                .payload(event.toByteArray())
                .attachTimestamp(attachTimestamp)
                .build()
        )
        return event
    }

    override fun activate() {
        active = true
    }

    override fun deactivate() {
        active = false
    }

    override fun isActive(): Boolean {
        return active
    }
}
