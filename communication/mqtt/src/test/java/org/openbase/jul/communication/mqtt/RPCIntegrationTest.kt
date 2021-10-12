package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.MqttClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5AsyncClient
import com.hivemq.client.mqtt.mqtt5.Mqtt5Client
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.*
import java.util.concurrent.TimeUnit

@Testcontainers
class RPCIntegrationTest {

    // the companion object makes sure that the container once before all tests instead of restarting for every test
    companion object {
        @Container
        var broker: MqttBrokerContainer = MqttBrokerContainer()
            .withEnv("DOCKER_VERNEMQ_ACCEPT_EULA", "yes")
            .withEnv("DOCKER_VERNEMQ_LISTENER.tcp.allowed_protocol_versions", "5") // enable mqtt5
            .withEnv("DOCKER_VERNEMQ_ALLOW_ANONYMOUS", "on") // enable connection without password
            .withExposedPorts(1883)
    }

    private val topic = "/test/integration"
    private val client = MqttClient.builder()
        .identifier(UUID.randomUUID().toString())
        .serverHost(broker.host)
        .serverPort(broker.firstMappedPort)
        .useMqttVersion5()
        .buildAsync();

    init {
        client.connect()[1, TimeUnit.SECONDS]
    }

    internal class Adder {
        fun add(a: Int, b: Int): Int {
            return a + b
        }
    }

    @Test
    fun test() {
        val instance = Adder()
        val method = Adder::class.java.getMethod("add", Int::class.java, Int::class.java)

        val rpcServer = RPCServer(client, topic)
        rpcServer.addMethod(method, instance)
        rpcServer.activate().get()

        val rpcClient = RPCRemote(client, topic)

        val expectedResult = 45
        val result = rpcClient.callMethod("add", Int::class.java, 3, 42).get(1, TimeUnit.SECONDS)

        result shouldBe expectedResult
    }
}

// this is needed because the generics used in GenericContainer do not allow to create a container directly in kotlin
class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("vernemq/vernemq"))