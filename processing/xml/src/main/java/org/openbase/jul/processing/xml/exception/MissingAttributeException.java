package org.openbase.jul.processing.xml.exception;

/*
 * #%L
 * JUL Processing XML
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import nu.xom.Element;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class MissingAttributeException extends XMLParsingException {

	public MissingAttributeException(final String attributeName, final Element sourceElement, final Exception cause) {
		super("Missing Attribute["+attributeName+"] for Element["+sourceElement.getQualifiedName()+"].", cause);
	}

	public MissingAttributeException(final String attributeName, final Element sourceElement) {
		super("Missing Attribute["+attributeName+"] for Element["+sourceElement.getQualifiedName()+"].");
	}
}