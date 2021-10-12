package org.openbase.jul.communication.mqtt.jp

import org.openbase.jps.preset.AbstractJPString

class JPMqttBrokerHost : AbstractJPString(arrayOf("--broker-host")) {

    override fun getPropertyDefaultValue(): String {
        return "localhost"
    }

    override fun getDescription(): String {
        return "The hostname of the machine running the MQTT Broker"
    }
}