package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.Communicator
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.ScopeType.Scope
import java.util.*

abstract class CommunicatorImpl(
    final override val scope: Scope,
    final override val config: CommunicatorConfig
) : Communicator {

    final override val id: UUID = UUID.randomUUID()
    val mqttClient: Mqtt5AsyncClient = SharedMqttClient.get(config)
    open val topic: String = ScopeProcessor.generateStringRep(scope)

    override fun waitForShutdown() {
        SharedMqttClient.waitForShutdown()
    }
}
