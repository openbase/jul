package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.openbase.jul.communication.mqtt.test.MqttBrokerManager
import org.testcontainers.junit.jupiter.Testcontainers

//@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class AbstractIntegrationTest {

    @BeforeAll
    @Timeout(30)
    fun setupMqtt() {
        MqttBrokerManager.setupMqtt(this::class.java.simpleName)
    }

    @AfterAll
    @Timeout(30)
    fun cleanup() {
        MqttBrokerManager.tearDownMQTT()
    }

    protected val brokerHost: String? get() = MqttBrokerManager.broker?.host

    protected val brokerPort: Int? get() = MqttBrokerManager.broker?.firstMappedPort
}

fun Mqtt5Publish.clearTimestamp() = let {
    this.extend().userProperties(Mqtt5UserProperties.of()).build()
}
