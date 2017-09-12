/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.extension.tcp.execution.clientCommand;

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
import org.openbase.jul.extension.tcp.execution.DialogMessageCommand;

/**
 *
 * @author divine
 */
public class ClientDialogMessageCommand extends DialogMessageCommand {

	// JSon Constructor
	public ClientDialogMessageCommand() {
		super();
	}
	
	public ClientDialogMessageCommand(String text, UserConfig userConfig) throws ConstructionException {
		super(userConfig.getResourceKey(), new Message(text, SERVER_NAME, userConfig.getName()));
	}
}
