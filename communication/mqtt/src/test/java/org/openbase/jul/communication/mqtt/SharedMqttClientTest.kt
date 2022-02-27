package org.openbase.jul.communication.mqtt

import org.junit.jupiter.api.Test

import org.openbase.jul.communication.config.CommunicatorConfig

internal class SharedMqttClientTest {

    @Test
    fun `shutdown should be work as expected`() {
        val config = CommunicatorConfig(IntegrationTest.broker.host, IntegrationTest.broker.firstMappedPort)
        val client = SharedMqttClient
        client.get(config).apply {
            disconnect()
        }
        client.waitForShutdown()
        client.shutdown()
    }
}
