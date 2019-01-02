package org.openbase.jul.extension.rsb.com.jp;

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
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPString;
import rsb.Factory;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;

/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPRSBHost extends AbstractJPString {

    public final static String[] COMMAND_IDENTIFIERS = {"--rsb-host"};

    public JPRSBHost() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected String getPropertyDefaultValue() throws JPNotAvailableException {
        return loadHostFromParticipantConfig();
    }

    @Override
    public String getDescription() {
        return "Setup the rsb host which is used by the application.";
    }

    private String loadHostFromParticipantConfig() {
        ParticipantConfig defaultParticipantConfig = Factory.getInstance().getDefaultParticipantConfig();
        for (TransportConfig config : defaultParticipantConfig.getTransports().values()) {

            if (!config.isEnabled()) {
                continue;
            }

            final String hostProperty = "transport." + config.getName() + ".host";

            // load host
            if (config.getOptions().hasProperty(hostProperty)) {
                return config.getOptions().getProperty(hostProperty).asString();
            }

        }
        
        // return default host
        return "localhost";
    }
}
