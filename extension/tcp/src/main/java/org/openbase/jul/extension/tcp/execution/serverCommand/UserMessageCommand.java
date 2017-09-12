/**
 * 
 */
package org.openbase.jul.extension.tcp.execution.serverCommand;

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

import org.openbase.jul.extension.tcp.Message;
import org.openbase.jul.extension.tcp.execution.AbstractCommand;

/**
 * @author divine
 *
 */
public class UserMessageCommand extends AbstractCommand {
	

	private final Message message;

	/**
	 * JSON Constructor
	 */
	private UserMessageCommand() {
		message = new Message();
	}	
	
	/**
	 * 
	 */
	public UserMessageCommand(Message message) {
		super(AbstractCommand.SEND_AGAIN_BY_TRANSMIT_FAIL);
		this.message = message;
		
	}
	
	public Message getMessage() {
		return message;
	}
}
