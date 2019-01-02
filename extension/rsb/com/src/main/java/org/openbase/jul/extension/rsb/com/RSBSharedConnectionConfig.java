package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
 */
import org.slf4j.LoggerFactory;
import rsb.config.ParticipantConfig;
import rsb.transport.spread.InPushConnectorFactoryRegistry;
import rsb.transport.spread.SharedInPushConnectorFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RSBSharedConnectionConfig {

    private final static org.slf4j.Logger LOGGER = LoggerFactory.getLogger(RSBSharedConnectionConfig.class);

    private static final String KEY_IN_PUSH_FACTORY = "shareIfPossible";

    private static boolean init = false;
    private static boolean initFactory = false;
    private static ParticipantConfig participantConfig;

    public synchronized static void load() {
        if (init) {
            return;
        }
        participantConfig = RSBDefaultConfig.getDefaultParticipantConfig();

        initInPusConnectorFactory();

        // instruct the spread transport to use your newly registered factory
        // for creating in push connector instances
        participantConfig.getOrCreateTransport("spread")
                .getOptions()
                .setProperty("transport.spread.java.infactory", KEY_IN_PUSH_FACTORY);
        
        init = true;
    }

    private static void initInPusConnectorFactory() {
        if(initFactory) {
            return;
        }

        // register a (spread-specifc) factory to create the appropriate in push
        // connectors. In this case the factory tries to share all connections
        // except the converters differ. You can implement other strategies to
        // better match your needs.
        InPushConnectorFactoryRegistry.getInstance().registerFactory(KEY_IN_PUSH_FACTORY, new SharedInPushConnectorFactory());

        initFactory = true;
    }

    public static ParticipantConfig getParticipantConfig() {
        if (!init) {
            load();
        }
        return participantConfig.copy();
    }

    public static synchronized void reload() {
        init = false;
        load();
    }
}
