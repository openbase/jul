package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class NotInitializedException extends InvalidStateException {

	public NotInitializedException(String message, Object context) {
		super("Given "+context+" is not initialized yet: "+message);
	}

	public NotInitializedException(Object context) {
		super("Given "+context+" is not initialized yet!");
	}

	public NotInitializedException(Object context, Throwable cause) {
		super("\"Given \"+context+\" is not initialized yet!", cause);
	}

	public NotInitializedException(String message,Object context, Throwable cause) {
		super("Given "+context+" is not initialized yet: "+message, cause);
	}
}
