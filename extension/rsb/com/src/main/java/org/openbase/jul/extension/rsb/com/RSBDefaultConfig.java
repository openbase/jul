package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.rsb.com.jp.JPRSBTransport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;

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

        // Disable rsb introspection for test mode.
        if (JPService.testMode()) {
            participantConfig.setIntrospectionEnabled(false);
        }

        Factory.getInstance().setDefaultParticipantConfig(participantConfig);
        init = true;
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
}
