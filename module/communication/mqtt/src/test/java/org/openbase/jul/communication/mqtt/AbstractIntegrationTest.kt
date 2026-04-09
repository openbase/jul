package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.mqtt.test.MqttBrokerManager
import org.testcontainers.junit.jupiter.Testcontainers

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class AbstractIntegrationTest {

    @BeforeAll
    @Timeout(30)
    fun setupMqtt() {
        MqttBrokerManager.setupMqtt(this::class.java.simpleName)
    }

    protected val brokerHost: String? get() = MqttBrokerManager.broker?.host

    protected val brokerPort: Int? get() = MqttBrokerManager.broker?.firstMappedPort

    protected val config
        get() = CommunicatorConfig(
            hostname = brokerHost ?: error("Host not defined!"),
            port = brokerPort ?: error("Port not defined!"),
        )
}

fun Mqtt5Publish.clearTimestamp() = let {
    this.extend().userProperties(Mqtt5UserProperties.of()).build()
}
