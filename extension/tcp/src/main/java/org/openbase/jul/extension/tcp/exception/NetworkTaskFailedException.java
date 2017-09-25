/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.openbase.jul.extension.tcp.exception;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.tcp.execution.AbstractCommand;

/**
 *
 * @author divine
 */
public class NetworkTaskFailedException extends CouldNotPerformException {
	

	AbstractCommand command;
	
	public NetworkTaskFailedException(String message, AbstractCommand command, Throwable cause) {
		super("Could not execute " + command.getName() + ". " + message, cause);
		this.command = command;
	}
	
	public NetworkTaskFailedException(AbstractCommand command, Throwable cause) {
		super("Could not execute " + command.getName() + ". " + cause.getMessage(), cause);
		this.command = command;
	}

    public AbstractCommand getCommand() {
		return command;
	}
}
