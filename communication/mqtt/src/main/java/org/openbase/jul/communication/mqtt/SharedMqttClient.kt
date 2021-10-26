package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import org.openbase.jul.communication.config.CommunicatorConfig
import java.util.*

object SharedMqttClient {

    private var sharedClients: MutableMap<CommunicatorConfig, Mqtt5AsyncClient> = mutableMapOf()

    @Synchronized
    fun get(communicatorConfig: CommunicatorConfig): Mqtt5AsyncClient {
        if (!sharedClients.containsKey(communicatorConfig)) {
            val client = MqttClient.builder()
                .identifier(UUID.randomUUID().toString())
                .serverHost(communicatorConfig.hostname)
                .serverPort(communicatorConfig.port)
                .useMqttVersion5()
                .buildAsync()
            client.connect().get()
            sharedClients[communicatorConfig] = client;
        }

        return sharedClients[communicatorConfig]!!
    }
}