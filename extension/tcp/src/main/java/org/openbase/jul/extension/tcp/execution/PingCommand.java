/**
 *
 */
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

import org.openbase.jul.extension.tcp.TCPConnection;

/**
 * @author divine
 *
 */
public class PingCommand extends AbstractCommand {

	
	private final long creationTimeStemp;
	private final long sourceID;

	/**
	 * JSON Constructor
	 */
	private PingCommand() {
		sourceID = -1;
		creationTimeStemp = -1;
	}

	/**
	 * JSON Constructor
	 */
	public PingCommand(TCPConnection sourceConnection) {
		super(AbstractCommand.DELETE_BY_TRANSMIT_FAIL);
		this.creationTimeStemp = System.currentTimeMillis();
		this.sourceID = sourceConnection.getSourceID();
	}

	public long getCreationTimeStemp() {
		return creationTimeStemp;
	}

	public long getSourceID() {
		return sourceID;
	}

	@Override
	public String toString() {
		if (getConnectionInfo() == null) {
			return getClass().getSimpleName() + ": unknown connectionInfo";
		}
		return getClass().getSimpleName()+ "[PingSource:" + getSourceID() + " | " + getConnectionInfo() + "]";
	}
}
