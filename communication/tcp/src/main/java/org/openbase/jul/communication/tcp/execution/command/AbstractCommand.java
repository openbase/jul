package org.openbase.jul.communication.tcp.execution.command;

/*-
 * #%L
 * JUL Extension TCP
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.openbase.jul.communication.tcp.datatype.ConnectionInfo;

import java.io.Serializable;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, property = "@class")
public abstract class AbstractCommand implements Serializable {

    @JsonIgnore
    protected final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(AbstractCommand.class);
    
	//TODO implement execution state

	public final static int MAX_SEND_TRIALS = 10;
	public final static boolean DELETE_BY_TRANSMIT_FAIL = true;
	public final static boolean SEND_AGAIN_BY_TRANSMIT_FAIL = false;
	private ConnectionInfo connectionInfo;
	private boolean executed;
	private final Object connectionLock = new Object();
	private boolean transmitted;
	private boolean deletByTransmitfailure;

	/**
	 * JSON Constructor
	 */
	protected AbstractCommand() {
	}

	public AbstractCommand(boolean transmitfailHandle) {
		this.executed = false;
		this.transmitted = false;
		this.connectionInfo = null;
		this.deletByTransmitfailure = transmitfailHandle;
	}

	public void setConnectionInfo(ConnectionInfo connectionInfo) {
		this.connectionInfo = connectionInfo;
	}

	public ConnectionInfo getConnectionInfo() {
		return connectionInfo;
	}

	@JsonIgnore
	public String getName() {
		return this.getClass().getSimpleName();
	}

	public void setExecuted(boolean executed) {
		this.executed = executed;
	}

	public boolean isExecuted() {
		return executed;
	}

	public void waitTillExecuted() {
		//TODO implemend future in network execution and register on execution complete event or throw exception in failed case.
		wait0fTransmit();
	}

	public void wait0fTransmit() {
		try {
			synchronized (connectionLock) {
				if (transmitted) {
					return;
				}
				connectionLock.wait();
			}
		} catch (InterruptedException ex) {
			ExceptionPrinter.printHistory("Wait of transmit interrupted!", ex, LOGGER, LogLevel.WARN);
		}
	}

	@JsonIgnore
	public void setTransmitted() {
		LOGGER.debug(this + "is transmitted.");
		synchronized (connectionLock) {
			transmitted = true;
			connectionLock.notifyAll();
		}
	}

	public boolean isTransmitted() {
		return transmitted;
	}

	public boolean isDeletByTransmitfailure() {
		return deletByTransmitfailure;
	}

	public boolean deletByTransmitfailure() {
		return deletByTransmitfailure;
	}

	@Override
	public String toString() {
		if (connectionInfo == null) {
			return getClass().getSimpleName() + ": unknown connectionInfo";
		}
		return getClass().getSimpleName() + "[" + connectionInfo + "]";
	}
}
