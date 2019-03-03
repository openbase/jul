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
import org.openbase.jul.exception.printer.ExceptionPrinter;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class TCPClientConnection extends TCPConnection {

    private final Socket socket;
    private final String clientName;
    private final int clientID;

    public TCPClientConnection(final int clientID, final int serverID, final String clientName, final Socket socket) {
        super(ConnectionSourceType.Server);
        this.clientID = clientID;
        this.clientName = clientName;
        this.socket = socket;
        this.sourceID = serverID;
    }

    public synchronized void autoConnectionhandling() {
        if (autoConnectionThread == null) {
            autoConnectionThread = new Thread(this, "AutoConnection");
            autoConnectionThread.start();
        }
    }

    @Override
    protected synchronized boolean connect() {

        logger.info("Connecting to Client " + clientName + " on " + socket.getInetAddress().getHostName());
        notifyConnecting();

        try {
            out = socket.getOutputStream();
            out.write(sourceID);
            out.write(clientID);
            out.flush();
        } catch (IOException ex) {
            ExceptionPrinter.printHistory("Couldn't create outputStream.", ex, logger);
            disconnect();
            return false;
        }

        try {
            in = socket.getInputStream();
        } catch (IOException ex) {
            ExceptionPrinter.printHistory("Couldn't create InputStream.", ex, logger);
            disconnect();
            return false;
        }

        setConnected(true);

        logger.info("Established connection to Client " + clientName + ".");
        return true;
    }

    @Override
    public synchronized void disconnect() {
        if (connected) {
            logger.info("Close connection to Client " + clientName + " on " + socket.getInetAddress().getHostName());
            setConnected(false);
            terminate = true;
            notifyConnectionClosed();
        }

        if (parser != null) {
            try {
                parser.close();
            } catch (IOException ex) {
                logger.debug("Could not close paser stream!", ex);
            }
            parser = null;
        }

        if (generator != null) {
            try {
                generator.close();
            } catch (IOException ex) {
                logger.debug("Could not close generator stream!", ex);
            }
            generator = null;
        }

        if (in != null) {
            try {
                in.close();
            } catch (IOException ex) {
                logger.debug("Could not close input stream!", ex);
            }
            in = null;
        }

        if (out != null) {
            try {
                out.close();
            } catch (IOException ex) {
                logger.debug("Could not close output stream!", ex);
            }
            out = null;
        }
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ex) {
                logger.debug("Could not close socket!", ex);
            }
        }
    }

    @Override
    protected int getTargetID() {
        return clientID;
    }
}
