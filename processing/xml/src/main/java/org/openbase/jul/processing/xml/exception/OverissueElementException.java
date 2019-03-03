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
import nu.xom.Elements;

/**
 *
 @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class OverissueElementException extends XMLParsingException {
    
	public OverissueElementException(final String elementName, final Elements childElements, final Element parent, final Exception cause) {
		super("Expected one Element["+elementName+"] but found " + childElements.size() + " childs of parent Element["+parent.getLocalName()+"].", cause);
	}

	public OverissueElementException(final String elementName, final Elements childElements, final Element parent) {
		super("Expected one Element["+elementName+"] but found " + childElements.size() + " childs of parent Element["+parent.getLocalName()+"].");
	}
}
