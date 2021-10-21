package org.openbase.jul.communication.config;

/*-
 * #%L
 * JUL Communication Default
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

public class CommunicatorConfig {
    private String hostname;
    private Integer port;

    public String getHostname() {
        return hostname;
    }

    public CommunicatorConfig setHostname(String hostname) {
        this.hostname = hostname;
        return this;
    }

    public Integer getPort() {
        return port;
    }

    public CommunicatorConfig setPort(Integer port) {
        this.port = port;
        return this;
    }
}
