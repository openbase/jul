package org.openbase.jul.communication.mqtt.test

import org.openbase.jps.core.JPService
import org.openbase.jps.exception.JPServiceException
import org.openbase.jul.communication.jp.JPComHost
import org.openbase.jul.communication.jp.JPComPort
import org.openbase.jul.communication.mqtt.SharedMqttClient
import org.testcontainers.containers.GenericContainer
import org.testcontainers.utility.DockerImageName
import org.testcontainers.utility.MountableFile
import java.nio.file.Files
import java.nio.file.Path
import java.time.Duration
import kotlin.concurrent.Volatile
import kotlin.io.path.deleteIfExists

object MqttBrokerManager {
    var broker: GenericContainer<*>? = null
    val lock = Any()

    fun setupMqtt(testClassName: String, port: Int = 1883) {
        synchronized(lock) {
            if (broker == null) {
                val mosquittoConfig: Path = Files.createTempFile("${testClassName}_mosquitto_", ".conf")
                Files.write(
                    mosquittoConfig, listOf(
                        "allow_anonymous true",
                        "listener $port"
                    )
                )
                MqttBrokerContainer()
                    .withExposedPorts(port)
                    .withCopyFileToContainer(
                        MountableFile.forHostPath(mosquittoConfig.toString()),
                        "/mosquitto/config/mosquitto.conf"
                    )
                    .apply { withStartupTimeout(Duration.ofSeconds(30)).start() }
                    .also { broker = it }
                    .also { setupProperties() }
                    .also {
                        // Add shutdown hook to stop broker at JVM exit
                        Runtime.getRuntime().addShutdownHook(Thread {
                            synchronized(lock) {
                                broker?.stop()
                            }
                        })
                    }
                mosquittoConfig.deleteIfExists()
            }
        }
    }

    @Throws(JPServiceException::class)
    private fun setupProperties() {
        JPService.reset()
        JPService.registerProperty(JPComPort::class.java, broker!!.firstMappedPort)
        JPService.registerProperty(JPComHost::class.java, broker!!.host)
        JPService.setupJUnitTestMode()
    }

    class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("eclipse-mosquitto"))
}
