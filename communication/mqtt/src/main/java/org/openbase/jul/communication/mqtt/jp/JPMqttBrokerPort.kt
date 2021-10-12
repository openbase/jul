package org.openbase.jul.communication.mqtt.jp

import org.openbase.jps.preset.AbstractJPInteger

class JPMqttBrokerPort : AbstractJPInteger(arrayOf("--broker-port")){

    override fun getPropertyDefaultValue(): Int {
        return 1883
    }

    override fun getDescription(): String {
        return "The port used to communicate with the MQTT Broker"
    }
}