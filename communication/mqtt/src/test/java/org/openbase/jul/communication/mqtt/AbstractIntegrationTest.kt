package org.openbase.jul.communication.mqtt

import org.junit.jupiter.api.AfterAll
import org.testcontainers.containers.BindMode
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import kotlin.io.path.absolute
import kotlin.io.path.deleteIfExists
import kotlin.io.path.writeLines

@Testcontainers
abstract class AbstractIntegrationTest {

    // the companion object makes sure that the container is started once before all tests instead of restarting for every test
    companion object {

        private const val port: Int = 1883

        private val mosquittoConfig = kotlin.io.path.createTempFile(prefix = "mosquitto_", suffix = ".conf")
        private var broker: MqttBrokerContainer

        init {
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
            broker.start()
        }

        @JvmStatic
        @AfterAll
        fun cleanup() {
            SharedMqttClient.waitForShutdown()
            broker.stop()
            mosquittoConfig.deleteIfExists()
        }
    }

    protected val brokerHost: String = broker.host

    protected val brokerPort: Int = broker.firstMappedPort
}

class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("eclipse-mosquitto"))