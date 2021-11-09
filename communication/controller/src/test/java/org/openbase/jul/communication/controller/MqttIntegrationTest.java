package org.openbase.jul.communication.controller;

import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.communication.jp.JPComHost;
import org.openbase.jul.communication.jp.JPComPort;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

public class MqttIntegrationTest {

    public static final int port = 1883;

    @ClassRule
    public static GenericContainer broker = new GenericContainer(DockerImageName.parse("vernemq/vernemq"))
            .withEnv("DOCKER_VERNEMQ_ACCEPT_EULA", "yes")
            .withEnv("DOCKER_VERNEMQ_LISTENER.tcp.allowed_protocol_versions", "5") // enable mqtt5
            .withEnv("DOCKER_VERNEMQ_ALLOW_ANONYMOUS", "on") // enable connection without password
            .withExposedPorts(port);

    @BeforeClass
    public static void setUpClass() throws JPServiceException, InterruptedException {
        JPService.registerProperty(JPComPort.class, broker.getFirstMappedPort());
        JPService.registerProperty(JPComHost.class, broker.getHost());
        JPService.setupJUnitTestMode();
    }
}
