package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5Publish
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.io.path.deleteIfExists
import kotlin.jvm.java

//@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
open class AbstractIntegrationTest {

    companion object {
        const val port = 1883
        var broker: GenericContainer<*>? = null
        val lock = Any()
    }

    private var usageCounter = 0

    @BeforeAll
    @Timeout(30)
    fun setupMqtt() {
        synchronized(lock) {
            if (usageCounter == 0) {
                val mosquittoConfig: Path = Files.createTempFile("${this::class.java.simpleName}_mosquitto_", ".conf")
                Files.write(
                    mosquittoConfig, listOf(
                        "allow_anonymous true",
                        "listener $port"
                    )
                )

                broker = MqttBrokerContainer()
                    .withExposedPorts(port)
                    .withCopyFileToContainer(
                        MountableFile.forHostPath(mosquittoConfig.toString()),
                        "/mosquitto/config/mosquitto.conf"
                    )
                    .apply { withStartupTimeout(Duration.ofSeconds(30)).start() }
                    .also {
                        if (broker?.takeIf { it.containerId != null } != null)
                            error("broker was already initialized!")
                    }
                    .also { broker = it }

                mosquittoConfig.deleteIfExists()
            }
            usageCounter++
        }
    }

    @AfterAll
    @Timeout(30)
    fun cleanup() {
        synchronized(lock) {
            SharedMqttClient.waitForShutdown()
            usageCounter--
            if (usageCounter == 0) {
                SharedMqttClient.waitForShutdown()
                broker?.stop()
            }
        }
    }

    class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("eclipse-mosquitto"))

    protected val brokerHost: String? get() = broker?.host

    protected val brokerPort: Int? get() = broker?.firstMappedPort
}

fun Mqtt5Publish.clearTimestamp() = let {
    this.extend().userProperties(Mqtt5UserProperties.of()).build()
}
