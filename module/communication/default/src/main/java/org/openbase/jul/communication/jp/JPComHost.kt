package org.openbase.jul.communication.jp

import org.openbase.jps.preset.AbstractJPString

class JPComHost : AbstractJPString(arrayOf("--host", "--com-hostname")) {

    override fun getPropertyDefaultValue(): String {
        return "localhost"
    }

    override fun getDescription(): String {
        return "The hostname of the machine running the MQTT Broker"
    }
}
