package org.openbase.jul.communication.mqtt

import org.openbase.jps.core.JPService
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.mqtt.jp.JPMqttBrokerHost
import org.openbase.jul.communication.mqtt.jp.JPMqttBrokerPort

class DefaultConnectorConfig {
    companion object {
        fun get(): CommunicatorConfig {
            return CommunicatorConfig(
                port = JPService.getValue(JPMqttBrokerPort::class.java),
                hostname = JPService.getValue(JPMqttBrokerHost::class.java)
            )
        }
    }
}