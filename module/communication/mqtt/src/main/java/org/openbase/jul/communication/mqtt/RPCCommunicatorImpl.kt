package org.openbase.jul.communication.mqtt

import com.hivemq.client.mqtt.mqtt5.datatypes.Mqtt5UserProperties
import com.hivemq.client.mqtt.mqtt5.message.publish.Mqtt5PublishBuilder
import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.RPCCommunicator
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.ScopeType
import java.time.Instant

abstract class RPCCommunicatorImpl(
    scope: ScopeType.Scope,
    config: CommunicatorConfig
) : CommunicatorImpl(scope, config), RPCCommunicator {

    final override val topic: String = "${super.topic}/rpc"
}

fun Mqtt5PublishBuilder.Complete.attachTimestamp(attachTimestamp: Boolean = true) = also {
    attachTimestamp.takeIf { it }
        ?.let {
        val now = Instant.now()
        this.userProperties(
            Mqtt5UserProperties.builder()
                .add("TIMESTAMP_MS", now.epochSecond.toString())
                .add("TIMESTAMP_NANO", now.nano.toString())
                .build()
        )
    }
}
