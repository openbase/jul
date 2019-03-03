package org.openbase.jul.communication.tcp;

/*-
 * #%L
 * JUL Extension TCP
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
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class TCPServerConnection extends TCPConnection {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(TCPServerConnection.class);

    private Socket serverSocket;
    private final List<TCPSocketConfig> socketList;
    private int serverID;

    public TCPServerConnection(List<TCPSocketConfig> sockedList) {
        super(ConnectionSourceType.Client);
        this.socketList = sockedList;
    }

    public TCPServerConnection(TCPSocketConfig socked) {
        super(ConnectionSourceType.Client);
        this.socketList = new ArrayList<TCPSocketConfig>();
        this.socketList.add(socked);
    }

    public TCPServerConnection(String hostName, int port) {
        super(ConnectionSourceType.Client);
        this.socketList = new ArrayList<TCPSocketConfig>();
        this.socketList.add(new TCPSocketConfig(hostName, port));
    }

    public synchronized void autoConnect() {
        if (autoConnectionThread == null) {
            autoConnectionThread = new Thread(this, "AutoConnection");
            autoConnectionThread.start();
        }
    }

    private Socket connectToBestSocket(final List<TCPSocketConfig> socketList) throws CouldNotPerformException {
        for (TCPSocketConfig config : socketList) {
            LOGGER.info("Try to connect to " + config.getHost() + "...");
            notifyConnecting();

            try {
                return config.getSocket();
            } catch (final Exception ex) {
                ExceptionPrinter.printHistory("Could not connect to " + config.getHost() + ".", ex, LOGGER);
                continue;
            }
        }
        throw new CouldNotPerformException("Could not find any route to server.");
    }

    @Override
    protected synchronized boolean connect() {

        try {
            serverSocket = connectToBestSocket(socketList);
        } catch (final CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could'n connect.", ex, LOGGER, LogLevel.WARN);
            notifyConnectionNoRouteToHost();
            return false;
        }

        try {
            in = serverSocket.getInputStream();
            serverID = in.read();
            sourceID = in.read();
            LOGGER.debug("Client got id " + sourceID);
        } catch (IOException ex) {
            ExceptionPrinter.printHistory("Could'n create InputBuffer.", ex, LOGGER);
            disconnect();
            return false;
        }

        try {
            out = serverSocket.getOutputStream();
        } catch (IOException ex) {
            ExceptionPrinter.printHistory("Could'n create OutputBuffer.", ex, LOGGER);
            disconnect();
            return false;
        }

        setConnected(true);
        LOGGER.info("Connection established.");
        return true;
    }

    @Override
    protected synchronized void disconnect() {
        if (connected) {
            LOGGER.info("Close connection to Server.");
            setConnected(false);
            notifyConnectionClosed();
        }

        if (parser != null) {
            try {
                parser.close();
            } catch (IOException ex) {
                LOGGER.debug("Could not close paser stream!", ex);
            }
            parser = null;
        }

        if (generator != null) {
            try {
                generator.close();
            } catch (IOException ex) {
                LOGGER.debug("Could not close generator stream!", ex);
            }
            generator = null;
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                LOGGER.debug("Could not close input stream!", ex);
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
                LOGGER.debug("Could not close output stream!", ex);
            }
            out = null;
        }

        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException ex) {
                LOGGER.debug("Could not close serversocket!", ex);
            }
            serverSocket = null;
        }
    }

    public String getHostName() {
        return serverSocket.getInetAddress().getHostName();
    }

    /**
     * Returns the connection port.
     *
     * @return the port number
     */
    public int getPort() {
        return serverSocket.getPort();
    }

    @Override
    protected int getTargetID() {
        return serverID;
    }

    protected abstract void notifyConnectionNoRouteToHost();
}
