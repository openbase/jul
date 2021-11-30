package org.openbase.jul.communication.jp

import org.openbase.jps.preset.AbstractJPInteger

class JPComPort : AbstractJPInteger(arrayOf("--com-port")){

    override fun getPropertyDefaultValue(): Int {
        return 1883
    }

    override fun getDescription(): String {
        return "The port used to communicate with the MQTT Broker"
    }
}
