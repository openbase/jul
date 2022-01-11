package org.openbase.jul.communication.tcp.execution.clientCommand;

/*-
 * #%L
 * JUL Extension TCP
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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
import org.openbase.jul.communication.tcp.datatype.Message;
import org.openbase.jul.communication.tcp.execution.command.AbstractCommand;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ClientMessageCommand extends AbstractCommand {

    private final Message message;

    /**
     * JSON Constructor
     */
    public ClientMessageCommand() {
        message = new Message();
    }

    public ClientMessageCommand(Message message) {
        super(AbstractCommand.SEND_AGAIN_BY_TRANSMIT_FAIL);
        this.message = message;

    }

    public Message getMessage() {
        return message;
    }
}
