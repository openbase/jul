package org.openbase.jul.extension.tcp.execution;

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
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.extension.tcp.TCPConnection;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class CommandExecuterThread {

    protected final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(CommandExecuterThread.class);

    private final Object lock;
    private Thread executer;
    private final AbstractCommand command;
    private final TCPConnection connection;

    public CommandExecuterThread(AbstractCommand command, TCPConnection connection) {
        this.command = command;
        this.connection = connection;
        this.lock = new Object();
    }

    public void start() {
        synchronized (lock) {
            if (executer != null && !executer.isInterrupted()) {
                return;
            }

            this.executer = new Thread(command.getClass().getSimpleName() + "Executer") {

                @Override
                public void run() {
                    try {
                        execute(command);
                    } catch (InvalidStateException ex) {
                        ExceptionPrinter.printHistory("Version error during execution! Please check server and client version!", ex, LOGGER);
                        connection.connectionError("Client version out of date!");
                    } catch (ClassNotFoundException ex) {
                        ExceptionPrinter.printHistory("Version error during execution! Please check server and client version!", ex, LOGGER);
                        connection.connectionError("Client version out of date!");
                    } catch (Exception ex) {
                        ExceptionPrinter.printHistory("Error during execution!", ex, LOGGER);
                    }
                }
            };
            executer.start();
        }
    }

    public void cancel() {
        synchronized (lock) {
            if (executer != null) {
                executer.interrupt();
            }
        }
    }

    public abstract void execute(AbstractCommand command) throws ClassNotFoundException, InvalidStateException, Exception;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[execute:" + command + "]";
    }
}
