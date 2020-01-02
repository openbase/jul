package org.openbase.jul.communication.tcp.execution.command;

/*-
 * #%L
 * JUL Extension TCP
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.openbase.jul.communication.tcp.TCPConnection;
import org.slf4j.LoggerFactory;


/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <T>
 * @param <C>
 */
public abstract class AbstractCommandExecuter<T extends AbstractCommand, C extends TCPConnection> {
	
    protected final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(getClass());
    
	protected T command;

	@JsonIgnore
	protected C connection;
	
	public AbstractCommandExecuter(T command, C connection) {
		this.command = command;
		this.connection = connection;
	}
	
	public abstract void execute() throws Exception;
	
	public void setCommand(T command) {
		this.command = command;
	}

	public T getCommand() {
		return command;
	}

	public C getConnection() {
		return connection;
	}

	@Override
	public String toString() {
		return "executer[Class: +"+this.getClass().getSimpleName()+"+  | Command:"+command+"]";
	}
}
