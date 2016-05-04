package org.dc.jul.extension.rsb.com.jp;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPNotAvailableException;
import org.dc.jps.preset.AbstractJPEnum;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class JPRSBTransport extends AbstractJPEnum<JPRSBTransport.TransportType> {

    public final static String[] COMMAND_IDENTIFIERS = {"--transport"};

    public JPRSBTransport() {
        super(COMMAND_IDENTIFIERS);
    }

    public enum TransportType {

        DEFAULT,
        SPREAD,
        SOCKET,
        INPROCESS;
    }

    @Override
    protected TransportType getPropertyDefaultValue() throws JPNotAvailableException {
        if (JPService.testMode()) {
            return TransportType.INPROCESS;
        }
        return TransportType.DEFAULT;
    }

    @Override
    public String getDescription() {
        return "Setup the rsb transport type which is used by the application.";
    }

}
