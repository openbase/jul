package org.openbase.jul.extension.rsb.com.jp;

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
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPInteger;
import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;

/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPRSBPort extends AbstractJPInteger {

    public final static String[] COMMAND_IDENTIFIERS = {"--rsb-port"};

    public JPRSBPort() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected Integer getPropertyDefaultValue() throws JPNotAvailableException {
        return loadPortFromParticipantConfig();
    }

    @Override
    public String getDescription() {
        return "Setup the rsb port which is used by the application.";
    }
    
    private int loadPortFromParticipantConfig() {
        ParticipantConfig defaultParticipantConfig = Factory.getInstance().getDefaultParticipantConfig();
        for (TransportConfig config : defaultParticipantConfig.getTransports().values()) {

            if (!config.isEnabled()) {
                continue;
            }

            final String portProperty = "transport." + config.getName() + ".port";

            // load host
            if (config.getOptions().hasProperty(portProperty)) {
                return config.getOptions().getProperty(portProperty).asInteger();
            }

        }
        
        // return default host
        return 4803;
    }

}
