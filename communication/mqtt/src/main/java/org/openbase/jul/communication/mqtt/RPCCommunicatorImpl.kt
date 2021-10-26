package org.openbase.jul.communication.mqtt

import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.RPCCommunicator
import org.openbase.jul.extension.type.processing.ScopeProcessor
import org.openbase.type.communication.ScopeType

abstract class RPCCommunicatorImpl(
    override val scope: ScopeType.Scope,
    override val config: CommunicatorConfig
) : CommunicatorImpl(scope, config), RPCCommunicator {
    override val topic: String = "${ScopeProcessor.generateStringRep(scope)}/rpc"
}