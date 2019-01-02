package org.openbase.jul.extension.tcp;

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
import org.openbase.jul.extension.tcp.datatype.ConnectionInfo;
import org.openbase.jul.extension.tcp.databind.ClassKeyMapperModule;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.openbase.jul.extension.tcp.execution.command.AbstractCommand;
import org.openbase.jul.extension.tcp.execution.command.ByeCommand;
import org.openbase.jul.extension.tcp.execution.command.CommandExecuterThread;
import org.openbase.jul.extension.tcp.execution.command.PingCommand;
import org.openbase.jul.extension.tcp.execution.command.PingExecuter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class TCPConnection implements Runnable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public enum ConnectionSourceType {

        Server, Client
    };
    public final static int HEARD_BEAT_FREQ = 30000; // Millisecunds
    public final static int COMMUNICATION_FREQUENZEPORT = 20; // Millisecunds
    public final static int RECONNECTION_TIME = 10000; // Millisecunds
    public final static int CONNECTION_TIMEOUT = 15000; // Millisecunds
    public final static int BUFFER_SIZE = 512;
    public final static int PACKET_SIZE = 256;
    private boolean connectionTerminator;
    protected int sourceID;
    protected final ConnectionSourceType sourceType;
    private final Object outgoingCommandsLock = new Object();
    protected boolean connected;
    protected boolean terminate;
    private final List<AbstractCommand> outgoingCommands;
    protected InputStream in;
    protected OutputStream out;
    protected Thread autoConnectionThread, analyseInputThread, handelOutputThread;
    private long lastCommunication;
    private final Object waitTillNextBeat;
    private final ObjectMapper mapper;
    protected JsonParser parser;
    protected JsonGenerator generator;
    protected JsonFactory jsonFactory;
    private final Timeout timeOut;
    private long delay;

    public TCPConnection(final ConnectionSourceType sourceType) {
        this.sourceType = sourceType;
        this.connected = false;
        this.terminate = false;
        this.connectionTerminator = false;
        this.outgoingCommands = new ArrayList<>();
        this.lastCommunication = 0;
        this.delay = 0;
        this.waitTillNextBeat = new Object();

        this.jsonFactory = new JsonFactory();
        this.jsonFactory.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false); // disable auto-close of the outputStream
        this.mapper = new ObjectMapper(jsonFactory);
        this.mapper.enableDefaultTyping(); // default to using DefaultTyping.OBJECT_AND_NON_CONCRETE
        this.mapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        this.mapper.configure(JsonGenerator.Feature.AUTO_CLOSE_TARGET, false);  // paranoidly repeat ourselves
        this.mapper.disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
        new ClassKeyMapperModule(mapper);

        this.timeOut = new Timeout(CONNECTION_TIMEOUT) {
            @Override
            public void expired() {
                if (connected) {
                    logger.warn("Connection timeout expired!");
                    disconnect();
                }
            }
        };
    }

    @Override
    public void run() {
        while (!terminate) {
            logger.debug("Initialize TCP connection.");
            if (connect()) {
                analyseInputThread = new Thread(
                        () -> {
                            analyseInput();
                        }, "AnalyseInput");
                handelOutputThread = new Thread(
                        () -> {
                            handelOutput();
                        }, "HandelOutput");

                analyseInputThread.start();
                handelOutputThread.start();

                try {
                    analyseInputThread.join();
                } catch (InterruptedException ex) {
                    ExceptionPrinter.printHistory("Could not join analyseInputThread.", ex, logger, LogLevel.WARN);
                    disconnect();
                }
                try {
                    handelOutputThread.join();
                } catch (InterruptedException ex) {
                    ExceptionPrinter.printHistory("Could not join handelOutputThread.", ex, logger, LogLevel.WARN);
                    disconnect();
                }
            }

            if (sourceType == ConnectionSourceType.Client) {
                if (terminate) {
                    break;
                }
                try {
                    logger.info("Try to reconnect to in " + RECONNECTION_TIME / 1000 + " secunds.");
                    Thread.sleep(RECONNECTION_TIME);
                } catch (InterruptedException ex) {
                    ExceptionPrinter.printHistory("Reconnect interrupted!", ex, logger);
                    Thread.currentThread().interrupt();
                }
            }
        }
        autoConnectionThread = null;
    }

    public void finishConnection() {
        connectionTerminator = true;
        if (isConnected()) {
            AbstractCommand byeCommand = new ByeCommand();
            try {
                sendCommand(byeCommand);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory("Could not finish connection properly!", ex, logger);
                close();
            }
        } else {
            close();
        }
        try {
            autoConnectionThread.join();
        } catch (InterruptedException ex) {
            ExceptionPrinter.printHistory("Coudn't wait for connection finalisation.", ex, logger);
        }
    }

    public void connectionError(String errorMessage) {
        notifyConnectionError(errorMessage);
        close();
    }

    public void close() {
        logger.debug("Close connection...");
        terminate = true;
        timeOut.cancel();
        synchronized (waitTillNextBeat) {
            waitTillNextBeat.notifyAll();
        }
        disconnect();
    }

    public AbstractCommand sendCommand(AbstractCommand command) throws CouldNotPerformException { //TODO implement with future obejct
        if (command == null) {
            throw new CouldNotPerformException("Could not send command!", new NotAvailableException("Command"));
        }

        command.setConnectionInfo(getConnectionInfo());

        synchronized (outgoingCommandsLock) {
            outgoingCommands.add(command);
        }
        synchronized (waitTillNextBeat) {
            waitTillNextBeat.notifyAll();
        }
        return command;
    }

    private void analyseInput() {
        AbstractCommand newCommand;
        try {
            parser = jsonFactory.createParser(in);
            while (connected) {
                try {
                    logger.debug("wait for next command...");
                    newCommand = mapper.readValue(parser, AbstractCommand.class);
                    lastCommunication = System.currentTimeMillis();
                    timeOut.cancel();
                    logger.debug("New incomming command: " + newCommand);
                } catch (NullPointerException ex) {
                    ExceptionPrinter.printHistory("Connection lost!", ex, logger, LogLevel.WARN);
                    notifyConnectionError("Connection lost!");
                    disconnect();
                    continue;
                } catch (JsonMappingException ex) {
                    ExceptionPrinter.printHistory("Connection closed unexpected!", ex, logger, LogLevel.WARN);
                    notifyConnectionError("Connection broken!");
                    disconnect();
                    continue;
                } catch (JsonParseException ex) {
                    ExceptionPrinter.printHistory("Connection closed unexpected!", ex, logger, LogLevel.WARN);
                    notifyConnectionError("Programm version may out of date!");
                    disconnect();
                    continue;
                } catch (JsonProcessingException ex) {
                    ExceptionPrinter.printHistory("Connection closed unexpected!", ex, logger, LogLevel.WARN);
                    notifyConnectionError("Connection broken!");
                    disconnect();
                    continue;
                } catch (SocketException ex) {
                    logger.info("Connection closed.", ex);
                    notifyConnectionError("Connection closed.");
                    disconnect();
                    continue;
                } catch (IOException ex) {
                    ExceptionPrinter.printHistory("Connection error!", ex, logger);
                    notifyConnectionError("Fatal connection error! Please contact developer!");
                    disconnect();
                    continue;
                }

                notifyInputActivity();
                if (newCommand == null) {
                    ExceptionPrinter.printHistory("Bad Incomming Data!", new CouldNotPerformException("Command"), logger);
                    continue;
                }

                new CommandExecuterThread(newCommand, this) {
                    @Override
                    public void execute(AbstractCommand command) throws Exception {

                        if (command instanceof PingCommand) { // handle ping
                            new PingExecuter((PingCommand) command, TCPConnection.this).execute();
                            return;
                        }
                        handleIncomingCommand(command);
                    }
                }.start();
            }
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Fatal connection error!", ex, logger);
            notifyConnectionError("Fatal connection error! Please contact developer!");
            disconnect();
        }
    }

    private void handelOutput() {
        AbstractCommand nextCommand;

        try {
            generator = jsonFactory.createGenerator(out);
            sendCommand(new PingCommand(this)); //TODO pinging seems to be buggy! Sometimes sends inifinity pings without timeout. Please check!
            while (connected) {
                while ((!outgoingCommands.isEmpty()) && connected) {
                    synchronized (outgoingCommandsLock) {
                        nextCommand = outgoingCommands.remove(0); // get first command
                    }
                    try {
                        logger.debug("Send Command: " + nextCommand);
                        try {
                            assert mapper != null;
                            assert out != null;
                            assert nextCommand != null;
                            mapper.writeValue(generator, nextCommand);
                        } catch (NullPointerException ex) {
                            ExceptionPrinter.printHistory("Connection lost! ", ex, logger);
                            disconnect();
                            if (!nextCommand.deletByTransmitfailure()) {
                                synchronized (outgoingCommandsLock) {
                                    outgoingCommands.add(nextCommand);
                                }
                            }
                            break;
                        }
                        nextCommand.setTransmitted();
                    } catch (Exception ex) {
                        ExceptionPrinter.printHistory("Connection error! ", ex, logger);
                        disconnect();
                        if (!nextCommand.deletByTransmitfailure()) {
                            synchronized (outgoingCommandsLock) {
                                outgoingCommands.add(nextCommand);
                            }
                        }
                        break;
                    }
                    notifyOutputActivity();
                }

                // no more commands to send...
                if (connected) {
                    try {
                        out.flush();
                    } catch (IOException ex) {
                        ExceptionPrinter.printHistory("Connection lost! ", ex, logger);
                        disconnect();
                        break;
                    }
                }
                synchronized (waitTillNextBeat) {
                    try {
                        waitTillNextBeat.wait(Math.max(HEARD_BEAT_FREQ - (System.currentTimeMillis() - lastCommunication), 0));
                    } catch (InterruptedException ex) {
                        continue;
                    }
                }
                if (!timeOut.isActive()) {
                    timeOut.start();
                    sendCommand(new PingCommand(this));
                }
            }
            logger.info("Communication finished.");
        } catch (Exception ex) {
            ExceptionPrinter.printHistory("Fatal connection error!", ex, logger);
            ex.printStackTrace(System.err);
            disconnect();
        }

    }

    public synchronized void analyseDelay(PingCommand command) {
        delay = System.currentTimeMillis() - command.getCreationTimeStemp();
        logger.debug("ConnectionDelay: " + delay);
        notifyConnectionDelay(delay);
    }

    public ConnectionInfo getConnectionInfo() {
        return new ConnectionInfo(sourceID, getTargetID());
    }

    public long getDelay() {
        return delay;
    }

    protected void setConnected(boolean connected) {
        if (this.connected == connected) {
            return;
        }
        this.connected = connected;
        notifyConnectionStateChanged();
    }

    public boolean isConnected() {
        return connected;
    }

    public boolean isConnectionTerminator() {
        return connectionTerminator;
    }

    public int getSourceID() {
        return sourceID;
    }

    public ConnectionSourceType getSourceType() {
        return sourceType;
    }

    protected abstract int getTargetID();

    protected abstract boolean connect();

    protected abstract void disconnect();

    protected abstract void notifyInputActivity();

    protected abstract void notifyOutputActivity();

    protected abstract void notifyConnectionDelay(long delay);

    protected abstract void notifyConnectionStateChanged();

    protected abstract void notifyConnecting();

    protected abstract void notifyConnectionClosed();

    protected abstract void notifyConnectionError(String errorMessage);

    protected abstract void handleIncomingCommand(AbstractCommand command) throws Exception;
}
