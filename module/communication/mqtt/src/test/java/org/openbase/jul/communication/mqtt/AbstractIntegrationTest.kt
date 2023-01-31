package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Timeout
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.time.Duration
import kotlin.io.path.absolute
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeLines

@Testcontainers
abstract class AbstractIntegrationTest {

    // the companion object makes sure that the container is started once before all tests instead of restarting for every test
    companion object {

        private const val port: Int = 1883

        private val mosquittoConfig = kotlin.io.path.createTempFile(prefix = "mosquitto_", suffix = ".conf")
        private lateinit var broker: MqttBrokerContainer

        private var usageCounter = 0
        private val lock = Any()

        @JvmStatic
        @BeforeAll
        @Timeout(30)
        fun setup() {
            synchronized(lock) {
                if (usageCounter == 0) {
                    mosquittoConfig.writeLines(
                        listOf(
                            "allow_anonymous true",
                            "listener 1883"
                        )
                    )

                    broker = MqttBrokerContainer()
                        .withExposedPorts(port)
                        .withFileSystemBind(
                            mosquittoConfig.absolute().toString(),
                            "/mosquitto/config/mosquitto.conf",
                            BindMode.READ_ONLY
                        )
                    broker.withStartupTimeout(Duration.ofSeconds(30)).start()
                }
                usageCounter++
            }
        }

        @JvmStatic
        @AfterAll
        @Timeout(30)
        fun cleanup() {
            synchronized(lock) {
                usageCounter--
                if (usageCounter == 0) {
                    SharedMqttClient.waitForShutdown()
                    broker.stop()
                    mosquittoConfig.deleteIfExists()
                }
            }
        }
    }

    protected val brokerHost: String get() = broker.host

    protected val brokerPort: Int get() = broker.firstMappedPort
}

class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("eclipse-mosquitto"))

fun Mqtt5Publish.clearTimestamp() = let {
    this.extend().userProperties(Mqtt5UserProperties.of()).build()
}
