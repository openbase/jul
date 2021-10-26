package org.openbase.jul.communication.mqtt

import org.openbase.jul.communication.config.CommunicatorConfig
import org.openbase.jul.communication.iface.*
import org.openbase.type.communication.ScopeType

class CommunicatorFactoryImpl: CommunicatorFactory {

    override fun createPublisher(scope: ScopeType.Scope, config: CommunicatorConfig): Publisher {
        return PublisherImpl(scope, config)
    }

    override fun createSubscriber(scope: ScopeType.Scope, config: CommunicatorConfig): Subscriber {
        return SubscriberImpl(scope, config)
    }

    override fun createRPCServer(scope: ScopeType.Scope, config: CommunicatorConfig): RPCServer {
        return RPCServerImpl(scope, config)
    }

    override fun createRPCClient(scope: ScopeType.Scope, config: CommunicatorConfig): RPCClient {
        return RPCClientImpl(scope, config)
    }
}