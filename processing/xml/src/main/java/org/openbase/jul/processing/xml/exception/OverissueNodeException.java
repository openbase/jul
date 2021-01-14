package org.openbase.jul.processing.xml.exception;

/*
 * #%L
 * JUL Processing XML
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

import nu.xom.Node;
import nu.xom.Nodes;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class OverissueNodeException extends XMLParsingException {

	public OverissueNodeException(final String nodeName, final Nodes childNodes, final Node parent, final Exception cause) {
		super("Expected one Node[" + nodeName + "] but found " + childNodes.size() + " childs of parent Element[" + parent.getBaseURI() + "].", parent.getBaseURI(), cause);
	}

	public OverissueNodeException(final String nodeName, final Nodes childElements, final Node parent) {
		super("Expected one Node[" + nodeName + "] but found " + childElements.size() + " childs of parent Element[" + parent.getBaseURI() + "].");
	}
}
