package org.openbase.jul.communication.mqtt.test

import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.Timeout
import org.junit.jupiter.api.extension.ExtendWith
import org.testcontainers.junit.jupiter.Testcontainers

/*-
 * #%L
 * JUL Extension Controller
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 *
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%

 * */
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@ExtendWith(OpenbaseDeadlockChecker::class)
@Testcontainers
open class MqttIntegrationTest {

    @BeforeAll
    @Timeout(30)
    fun setupMqtt() {
        MqttBrokerManager.setupMqtt(this::class.java.simpleName, 1884)
        setupCustomProperties()
    }

    @AfterAll
    @Timeout(30)
    fun tearDownMQTT() {
        MqttBrokerManager.tearDownMQTT()
    }

    open fun setupCustomProperties() {}
}
