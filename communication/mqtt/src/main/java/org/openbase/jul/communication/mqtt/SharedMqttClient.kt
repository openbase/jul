package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.iface.Shutdownable
import java.util.*

// TODO:
//  * Implement sharing correctly:
//    * Count instances accessing and disconnect if none is anymore
//    * save connecting future and make accessible
object SharedMqttClient: Shutdownable {

    private var sharedClients: MutableMap<CommunicatorConfig, Mqtt5AsyncClient> = mutableMapOf()

    init {
        Shutdownable.registerShutdownHook(this)
    }

    @Synchronized
    fun get(communicatorConfig: CommunicatorConfig): Mqtt5AsyncClient {
        if (!sharedClients.containsKey(communicatorConfig)) {
            val client = MqttClient.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(communicatorConfig.hostname)
                .serverPort(communicatorConfig.port)
                .useMqttVersion5()
                .buildAsync()
            client.connect()
            sharedClients[communicatorConfig] = client;
        }

        return sharedClients[communicatorConfig]!!
    }

    fun waitForShutdown() {
        sharedClients.values.forEach { client -> client.disconnect().get() }
    }

    override fun shutdown() {
        sharedClients.values.forEach { client -> client.disconnect() }
    }
}