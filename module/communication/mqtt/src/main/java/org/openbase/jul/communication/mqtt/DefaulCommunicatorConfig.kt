package org.openbase.jul.communication.mqtt

import org.openbase.jps.core.JPService
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.jp.JPComHost
import org.openbase.jul.communication.jp.JPComPort

class DefaultCommunicatorConfig {
    companion object {
        val instance get() = CommunicatorConfig(
                port = JPService.getValue(JPComPort::class.java),
                hostname = JPService.getValue(JPComHost::class.java)
            )
        }
    }
