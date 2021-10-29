package org.openbase.jul.communication.mqtt

import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.RPCCommunicator
import org.openbase.type.communication.ScopeType

abstract class RPCCommunicatorImpl(
    scope: ScopeType.Scope,
    config: CommunicatorConfig
) : CommunicatorImpl(scope, config), RPCCommunicator {

    final override val topic: String = "${super.topic}rpc"
}
