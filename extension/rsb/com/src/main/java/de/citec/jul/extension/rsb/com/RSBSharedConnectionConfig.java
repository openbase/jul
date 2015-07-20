/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.transport.spread.InPushConnectorFactoryRegistry;
import rsb.transport.spread.SharedInPushConnectorFactory;

/**
 *
 * @author mpohling
 */
public class RSBSharedConnectionConfig {

    private static boolean init = false;
    private static ParticipantConfig participantConfig;

    public synchronized static void load() {
        if (init) {
            return;
        }

        final String inPushFactoryKey = "shareIfPossible";
        // register a (spread-specifc) factory to create the appropriate in push
        // connectors. In this case the factory tries to share all connections
        // except the converters differ. You can implement other strategies to
        // better match your needs.
        InPushConnectorFactoryRegistry.getInstance().registerFactory(
                inPushFactoryKey, new SharedInPushConnectorFactory());

        // instruct the spread transport to use your newly registered factory
        // for creating in push connector instances
        participantConfig = Factory.getInstance().getDefaultParticipantConfig();
        participantConfig.getOrCreateTransport("spread")
                .getOptions()
                .setProperty("transport.spread.java.infactory", inPushFactoryKey);

        init = true;
    }

    public static ParticipantConfig getParticipantConfig() {
        return participantConfig;
    }
}
