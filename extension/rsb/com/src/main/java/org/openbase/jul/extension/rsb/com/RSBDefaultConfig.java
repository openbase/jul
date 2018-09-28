package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.rsb.com.jp.JPRSBHost;
import org.openbase.jul.extension.rsb.com.jp.JPRSBIntrospection;
import org.openbase.jul.extension.rsb.com.jp.JPRSBPort;
import org.openbase.jul.extension.rsb.com.jp.JPRSBThreadPooling;
import org.openbase.jul.extension.rsb.com.jp.JPRSBTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;
//import rsb.eventprocessing.ExecutorEventReceivingStrategyFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RSBDefaultConfig {

    protected static final Logger LOGGER = LoggerFactory.getLogger(RSBDefaultConfig.class);

    private static boolean init = false;
    private static ParticipantConfig participantConfig;

    public synchronized static void load() {
        if (init) {
            return;
        }
        participantConfig = Factory.getInstance().getDefaultParticipantConfig();

        try {
            // activate transport communication set by the JPRSBTransport property.
            enableTransport(participantConfig, JPService.getProperty(JPRSBTransport.class).getValue());
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), LOGGER);
        }

        try {
            // activate transport communication set by the JPRSBTransport property.
            setupHost(participantConfig, JPService.getProperty(JPRSBHost.class).getValue());
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), LOGGER);
        }

        try {
            // activate transport communication set by the JPRSBTransport property.
            setupPort(participantConfig, JPService.getProperty(JPRSBPort.class).getValue());
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), LOGGER);
        }

        // Setup introspection
        // Force disabling rsb introspection for test mode because the communication is done via socked transport which is not suitable for unit tests.
        if (JPService.testMode()) {
            participantConfig.setIntrospectionEnabled(false);
        } else {
            try {
                participantConfig.setIntrospectionEnabled(JPService.getProperty(JPRSBIntrospection.class).getValue());
            } catch (JPNotAvailableException ex) {
                ExceptionPrinter.printHistory("Could not check if introspection was enabled!", ex, LOGGER, LogLevel.WARN);
            }
        }

        try {
            if (JPService.getProperty(JPRSBThreadPooling.class).getValue()) {
                // todo reenable if pushed to official rsb version
                LOGGER.warn("RSB thread pooling not supported in this release!");
//                participantConfig.setReceivingStrategy(new ExecutorEventReceivingStrategyFactory(GlobalCachedExecutorService.getInstance().getExecutorService()));
            }
        } catch (JPNotAvailableException ex) {
            ExceptionPrinter.printHistory("Could not check if thread pooling was enabled!", ex, LOGGER, LogLevel.WARN);
        }

        Factory.getInstance().setDefaultParticipantConfig(participantConfig);
        init = true;
    }

    public static synchronized void reload() {
        init = false;
        load();
    }

    public static ParticipantConfig getDefaultParticipantConfig() {
        if (!init) {
            load();
        }
        return participantConfig.copy();
    }

    public static void enableTransport(final ParticipantConfig participantConfig, final JPRSBTransport.TransportType type) {
        if (type == JPRSBTransport.TransportType.DEFAULT) {
            return;
        }

        for (TransportConfig transport : participantConfig.getEnabledTransports()) {
            transport.setEnabled(false);
        }
        participantConfig.getOrCreateTransport(type.name().toLowerCase()).setEnabled(true);
    }

    public static void setupHost(final ParticipantConfig participantConfig, final String host) {
        for (TransportConfig config : participantConfig.getTransports().values()) {

            if (!config.isEnabled()) {
                continue;
            }

            final String hostProperty = "transport." + config.getName() + ".host";

            // remove configured host
            if (config.getOptions().hasProperty(hostProperty)) {
                config.getOptions().remove(hostProperty);
            }

            // setup host
            config.getOptions().setProperty(hostProperty, host);

        }
    }

    public static void setupPort(final ParticipantConfig participantConfig, final Integer port) {
        for (TransportConfig config : participantConfig.getTransports().values()) {

            if (!config.isEnabled()) {
                continue;
            }

            final String portProperty = "transport." + config.getName() + ".port";

            // remove configured host
            if (config.getOptions().hasProperty(portProperty)) {
                config.getOptions().remove(portProperty);
            }

            // setup host
            config.getOptions().setProperty(portProperty, Integer.toString(port));
        }
    }
}
