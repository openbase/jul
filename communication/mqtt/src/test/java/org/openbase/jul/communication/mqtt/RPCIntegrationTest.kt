package org.openbase.jul.communication.mqtt

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.testcontainers.containers.GenericContainer
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import org.testcontainers.utility.DockerImageName
import java.util.concurrent.TimeUnit

@Testcontainers
class RPCIntegrationTest {

    // the companion object makes sure that the container once before all tests instead of restarting for every test
    companion object {
        private const val port: Int = 1883

        @Container
        var broker: MqttBrokerContainer = MqttBrokerContainer()
            .withEnv("DOCKER_VERNEMQ_ACCEPT_EULA", "yes")
            .withEnv("DOCKER_VERNEMQ_LISTENER.tcp.allowed_protocol_versions", "5") // enable mqtt5
            .withEnv("DOCKER_VERNEMQ_ALLOW_ANONYMOUS", "on") // enable connection without password
            .withExposedPorts(port)
    }

    private val scope = ScopeProcessor.generateScope("/test/integration")
    private val config = CommunicatorConfig(broker.host, broker.firstMappedPort)

    internal class Adder {
        fun add(a: Int, b: Int): Int {
            return a + b
        }
    }

    @Test
    fun test() {
        val instance = Adder()

        val rpcServer = RPCServerImpl(scope, config)
        rpcServer.registerMethod(Adder::add, instance)
        rpcServer.activate()
        rpcServer.getActivationFuture()!!.get()

        val rpcClient = RPCClientImpl(scope, config)

        val expectedResult = 45
        val result = rpcClient.callMethod(instance::add.name, Int::class, 3, 42).get(1, TimeUnit.SECONDS)

        result shouldBe expectedResult
    }
}

// this is needed because the generics used in GenericContainer do not allow to create a container directly in kotlin
class MqttBrokerContainer : GenericContainer<MqttBrokerContainer>(DockerImageName.parse("vernemq/vernemq"))