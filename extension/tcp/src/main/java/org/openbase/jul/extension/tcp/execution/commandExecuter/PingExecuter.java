/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.execution.commandExecuter;

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

import org.openbase.jul.extension.tcp.TCPConnection;
import org.openbase.jul.extension.tcp.execution.AbstractCommandExecuter;
import org.openbase.jul.extension.tcp.execution.PingCommand;
import java.util.logging.Level;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;

/**
 *
 * @author divine
 */
public class PingExecuter extends AbstractCommandExecuter<PingCommand, TCPConnection> {

	public PingExecuter(PingCommand command, TCPConnection connection) {
		super(command, connection);
	}

	@Override
	public void execute() {
		if (command.getSourceID() == connection.getSourceID()) {
			connection.analyseDelay(command);
		} else {
			try {
				// send ping reply
				sleepBecauseOfPingBug(); // TODO Remove after bugfix.
				connection.sendCommand(command);
			} catch (CouldNotPerformException ex) {
				ExceptionPrinter.printHistory("Could not send pink reply!", ex, LOGGER, LogLevel.WARN);
			}
		}
	}


	public void sleepBecauseOfPingBug() {
		try {
			Thread.sleep(100);
		} catch (InterruptedException ex) {
			java.util.logging.Logger.getLogger(PingExecuter.class.getName()).log(Level.SEVERE, null, ex);
		}
	}
}
