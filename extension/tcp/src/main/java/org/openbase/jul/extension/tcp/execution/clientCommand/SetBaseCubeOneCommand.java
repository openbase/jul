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

import org.openbase.jul.extension.tcp.execution.AbstractCommand;
import org.openbase.jul.extension.tcp.wiretype.AbstractResourceConfig;

public class SetBaseCubeOneCommand extends AbstractCommand {

	private AbstractResourceConfig baseCubeOneConfig;
//	private byte[] baseCubeOneConfigBytes;

	/**
	 * JSON Constructor
	 */
	public SetBaseCubeOneCommand() {
	}

	public SetBaseCubeOneCommand(AbstractResourceConfig baseCubeOneConfig) {
		super(AbstractCommand.DELETE_BY_TRANSMIT_FAIL);
		this.baseCubeOneConfig = baseCubeOneConfig;
	}



	
//	public SetBaseCubeOneCommand(byte[] baseCubeOneConfigBytes) {
//		super(AbstractCommand.DELETE_BY_TRANSMIT_FAIL);
//		this.baseCubeOneConfigBytes = baseCubeOneConfigBytes;
//	}

	public AbstractResourceConfig getBaseCubeOneConfig() {
		return baseCubeOneConfig;
	}



//	public byte[] getBaseCubeOneConfigBytes() {
//		return baseCubeOneConfigBytes;
//	}
}
