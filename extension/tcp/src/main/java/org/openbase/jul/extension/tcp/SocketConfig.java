package org.openbase.jul.extension.tcp;

/*-
 * #%L
 * JUL Extension TCP
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
import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class SocketConfig {

    private final String host;
    private final int port;

    public SocketConfig(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public Socket getSocket() throws UnknownHostException, IOException {
        return new Socket(host, port);
    }

    public String getHost() {
        return host;
    }

    public int getPort() {
        return port;
    }
}
