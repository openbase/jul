package org.openbase.jul.processing.xml.processing;

/*
 * #%L
 * JUL Processing XML
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
import org.openbase.jul.processing.xml.exception.OverissueNodeException;
import org.openbase.jul.processing.xml.exception.NotOneNodeException;
import org.openbase.jul.processing.xml.exception.MissingNodeException;
import org.openbase.jul.processing.xml.exception.MissingElementException;
import org.openbase.jul.processing.xml.exception.OverissueElementException;
import org.openbase.jul.processing.xml.exception.MissingAttributeException;
import org.openbase.jul.processing.xml.exception.XMLParsingException;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import nu.xom.*;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotProcessException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

public class XMLProcessor {

    private static final org.slf4j.Logger LOGGER = LoggerFactory.getLogger(XMLProcessor.class);

    public enum NumberOfNodes {

        ARBITRARY, AT_LEAST_ONE, AT_MOST_ONE, EXACT_ONE
    }

    /**
     *
     * @param fileName
     * @return
     * @throws XMLParsingException
     * @throws IOException
     */
    public synchronized static Document createDocumentFromFile(final String fileName) throws XMLParsingException, IOException {
        Document doc = null;
        try {
            doc = new Builder().build(new File(fileName));
        } catch (ParsingException ex) {
            throw new XMLParsingException("Can not parse file " + fileName, ex);
        } catch (IOException ex) {
            throw new IOException("Can not access file " + fileName, ex);
        }
        return doc;
    }

    /**
     *
     * @param file
     * @return
     * @throws XMLParsingException
     * @throws IOException
     */
    public synchronized static Document createDocumentFromFile(final File file) throws XMLParsingException, IOException {
        Document doc = null;
        try {
            doc = new Builder().build(file);
        } catch (ParsingException ex) {
            throw new XMLParsingException("Can not parse file " + file.getAbsolutePath(), ex);
        } catch (IOException ex) {
            throw new IOException("Can not access file " + file.getAbsolutePath(), ex);
        }
        return doc;
    }

    /**
     *
     * @param xmlDoc
     * @return
     * @throws XMLParsingException
     */
    public static Document createDocumentFromString(final String xmlDoc) throws XMLParsingException {
        try {
            return new Builder().build(xmlDoc.trim(), "");
        } catch (ParsingException | IOException ex) {
            throw new XMLParsingException("Can not parse string " + xmlDoc, ex);
        }
    }

    /**
     *
     * @param xmlDoc
     * @return
     * @throws XMLParsingException
     */
    public static Element createElementFromString(final String xmlDoc) throws XMLParsingException {
        try {
            return new Builder().build(xmlDoc.trim(), "").getRootElement();
        } catch (ParsingException | IOException ex) {
            throw new XMLParsingException("Can not parse string " + xmlDoc, ex);
        }
    }

    public static Iterable<Element> toIterable(final Elements elements) {
        return () -> new Iterator<Element>() {
            private int i = 0;
            private final int size = elements.size();

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Element next() {
                return elements.get(i++);
            }

            @Override
            public void remove() {
                throw new AssertionError("Not supported for Elementterator!");
            }
        };
    }

    public static Iterable<Node> toIterable(final Nodes nodes) {
        return () -> new Iterator<Node>() {
            private int i = 0;
            private final int size = nodes.size();

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Node next() {
                return nodes.get(i++);
            }

            @Override
            public void remove() {
                nodes.remove(i++);
            }
        };
    }

    public static Iterable<Element> toIterableElement(final Nodes nodes) {
        return () -> new Iterator<Element>() {
            private int i = 0;
            private final int size = nodes.size();

            @Override
            public boolean hasNext() {
                return i < size;
            }

            @Override
            public Element next() {
                return (Element) nodes.get(i++);
            }

            @Override
            public void remove() {
                nodes.remove(i++);
            }
        };
    }

    /**
     *
     * @param nodeName
     * @param node
     * @param expectedNumberOfNodes
     * @return
     * @throws XMLParsingException
     */
    public static Nodes extractNodesByNodeName(final String nodeName, final Node node, final NumberOfNodes expectedNumberOfNodes) throws XMLParsingException {
        return extractNodesByXpath(".//" + nodeName, node, expectedNumberOfNodes);
    }

    public static Element extractOneChildElementByXPath(final String xPath, Node node) throws MissingNodeException, OverissueNodeException, NotOneNodeException {
        return (Element) extractNodesByXpath(xPath, node, NumberOfNodes.EXACT_ONE).get(0);
    }

    public static Nodes extractNodesByXpath(final String xpath, final Node node, final NumberOfNodes expectedNumberOfNodes) throws MissingNodeException, OverissueNodeException, NotOneNodeException {
        Nodes nodes = node.query(xpath);
        switch (expectedNumberOfNodes) {
            case ARBITRARY: {
                break;
            }
            case AT_LEAST_ONE: {
                if (nodes.size() < 1) {
                    throw new MissingNodeException(xpath, node);
                }
                break;

            }
            case AT_MOST_ONE: {
                if (nodes.size() > 1) {
                    throw new OverissueNodeException(xpath, nodes, node);
                }
                break;
            }
            case EXACT_ONE: {
                if (nodes.size() != 1) {
                    throw new NotOneNodeException(xpath, nodes, node);
                }
                break;
            }
            default:
                throw new AssertionError("Found not handled value[" + expectedNumberOfNodes.name() + "]!");
        }
        return nodes;
    }

    /**
     *
     * @param nodeName
     * @param node
     * @param expectedNumberOfNodes
     * @param throwException
     * @return
     * @throws XMLParsingException
     *
     */
    public static Nodes extractNodesByNodeName(final String nodeName, final Node node, final int expectedNumberOfNodes, final boolean throwException) throws XMLParsingException {
        return extractNodesByXpath(".//" + nodeName, node, expectedNumberOfNodes, throwException);
    }

    public static Nodes extractNodesByXpath(final String xpath, final Node node, final int expectedNumberOfNodes, final boolean throwException) throws XMLParsingException {
        Nodes nodes = node.query(xpath);
        if (nodes.size() == expectedNumberOfNodes) {
            return nodes;
        }

        if (throwException) {
            throw new XMLParsingException("Expected " + expectedNumberOfNodes + " to be found with xPath " + xpath + ", found " + nodes.size());
        }
        return nodes;
    }

    /**
     * <p>
     * Returns the first Attribute with name attrName from Document doc. Uses xPath "//
     *
     * @throws XMLParsingException
     * @param throwException flag set throw exception if no such Attribute can be found.	<br>
     * @param attrName
     * @param doc
     * @return
     */
    public static String extractAttributeValue(final String attrName, final Node doc, final boolean throwException) throws XMLParsingException {
        String xpath = "descendant-or-self::*/@" + attrName;
        Nodes nodes = doc.query(xpath);
        for (int i = 0; i < nodes.size(); i++) {
            if (nodes.get(i) instanceof Attribute) {
                return nodes.get(i).getValue();
            }
        }
        if (throwException) {
            throw new XMLParsingException("No Attribute " + attrName + " in document:\n" + doc.toXML());
        } else {
            return null;
        }
    }

    /**
     *
     * @param nodeName
     * @param doc
     * @param expectedNumberOfNodes
     * @return
     * @throws XMLParsingException
     */
    public static HashSet<String> extractValues(final String nodeName, final Node doc, final NumberOfNodes expectedNumberOfNodes) throws XMLParsingException {
        HashSet<String> names = new HashSet<>();
        Nodes nodes = extractNodesByNodeName(nodeName, doc, expectedNumberOfNodes);
        for (int i = 0; i < nodes.size(); i++) {
            names.add(nodes.get(i).getValue());
        }
        return names;
    }

    /**
     *
     * @param nodeName
     * @param doc
     * @param expectedNumberOfNodes
     * @param throwException
     * @return
     * @throws XMLParsingException
     */
    public static HashSet<String> extractValues(final String nodeName, final Node doc, final int expectedNumberOfNodes, final boolean throwException) throws XMLParsingException {
        HashSet<String> names = new HashSet<>();
        Nodes nodes = extractNodesByNodeName(nodeName, doc, expectedNumberOfNodes, throwException);
        for (int i = 0; i < nodes.size(); i++) {
            names.add(nodes.get(i).getValue());
        }
        return names;
    }

    /**
     * Does nothing if nodes contains at least one node. Throws InvalidCfgDocException otherwise.
     *
     * @param nodes
     * @param nodeNameForDebugging
     * @throws XMLParsingException
     */
    public void existenceCheck(final Nodes nodes, final String nodeNameForDebugging) throws XMLParsingException {
        if (nodes.size() == 0) {
            throw new XMLParsingException("Message doesn't contain a " + nodeNameForDebugging + "node!");
        }
    }

    /**
     * Does nothing if only one of nodes1 or nodes2 contains more than zero nodes. Throws InvalidCfgDocException otherwise.
     *
     * @param nodes1
     * @param nodes2
     * @param nodeTypeForDebugging
     * @throws XMLParsingException
     */
    public static void xorCheck(final Nodes nodes1, final Nodes nodes2, final String nodeTypeForDebugging) throws XMLParsingException {
        if (nodes1.size() > 0 && nodes2.size() > 0) {
            throw new XMLParsingException("Message contains more than one " + nodeTypeForDebugging + " node. Only one permitted.");
        }
    }

    /**
     * Does nothing if only one of nodes1, nodes2 or nodes3 contains more than zero nodes. Throws InvalidCfgDocException otherwise.
     *
     *
     * @param nodes1
     * @param nodes2
     * @param nodes3
     * @param nodeTypeForDebugging
     * @throws XMLParsingException
     */
    public static void xorCheck(final Nodes nodes1, final Nodes nodes2, final Nodes nodes3, final String nodeTypeForDebugging) throws XMLParsingException {
        if (nodes1.size() > 0 && nodes2.size() > 0 && nodes3.size() > 0) {
            throw new XMLParsingException("Message contains more than one " + nodeTypeForDebugging + " node. Only one permitted.");
        }
    }

    /**
     *
     * @param doc
     * @return
     */
    public static String serialize(final Node doc) throws CouldNotProcessException {
        final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
        final Serializer ser = new Serializer(outStream);
        try {
            ser.setIndent(3);
            ser.write(doc.getDocument());
            return outStream.toString();
        } catch (IOException ex) {
            throw new CouldNotProcessException("Couldn't transform doc to prettyXMLString. Returning old document.", ex);
        }
    }

    /**
     * Creates a pretty formatted version of doc. Note: does not remove line breaks!
     *
     * @param doc
     * @return
     */
    public static Node normalizeFormatting(final Node doc) throws CouldNotProcessException {
        try {
            return createDocumentFromString(normalizeFormattingAsString(doc));
        } catch (XMLParsingException ex) {
            throw new CouldNotProcessException("Couldn't normalize formatting. Returning old document.", ex);
        }
    }

    public static String normalizeFormattingAsString(final Node doc) {
        try {
            return serialize(skipNlTabWs(doc));
        } catch (Exception ex) {
            throw new CouldNotProcessException("Couldn't normalize formatting. Returning old document.", ex);
        }
    }

    /**
     *
     * @param doc
     * @return
     */
    public static Document normalizeFormatting(final Document doc) {
        return (Document) normalizeFormatting((Node) doc);
    }

    public static Nodes normalizeFormatting(final Nodes docs) {
        Nodes n = new Nodes();
        for (int i = 0; i < docs.size(); i++) {
            n.append(normalizeFormatting(docs.get(i)));
        }
        return n;
    }

    public static Node skipNlTabWs(final Node oldDoc) throws CouldNotProcessException {
        Node copy = oldDoc.copy();
        try {
            String oldString = oldDoc.toXML();
            String newString = oldString.replace("\n", "");
            newString = newString.replace("\t", "");
            newString = newString.trim();
            return createDocumentFromString(newString);
        } catch (Exception ex) {
            throw new CouldNotProcessException("Couldn't skipNlTabWs. Returning old document.", ex);
        }
    }

    protected Element getElementFromXPath(final String xPath, final Document document) throws VerificationFailedException {
        Nodes regNodes = document.query(xPath);
        if (regNodes.size() != 1) {
            throw new VerificationFailedException("Invalide data from XPath[" + xPath + "]!");
        }
        return (Element) regNodes.get(0);
    }

    protected static Iterator<Element> getElementsFromXPath(final String xPath, final Document document) {
        Nodes regNodes = document.query(xPath);
        List<Element> elements = new ArrayList<>();
        for (int i = 0; i < regNodes.size(); i++) {
            try {
                elements.add((Element) regNodes.get(i));
            } catch (ClassCastException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Invalide data from XPath[" + xPath + "]!", ex), LOGGER);
            }
        }
        return elements.iterator();
    }

    public static boolean parseBooleanElementValue(final String elementName, final Element sourceElement) {
        try {
            return Boolean.parseBoolean(XMLProcessor.parseOneChildElement(elementName, sourceElement).getValue());
        } catch (MissingElementException | OverissueElementException ex) {
            return false;
        }
    }

    public static Element parseOneChildElement(final String elementName, final Element sourceElement) throws MissingElementException, OverissueElementException {
        Elements elements = sourceElement.getChildElements(elementName);
        if (elements.size() == 0) {
            throw new MissingElementException(elementName, sourceElement);
        } else if (elements.size() != 1) {
            throw new OverissueElementException(elementName, elements, sourceElement);
        }
        return elements.get(0);
    }

    public static String parseAttributeValue(final String attributeName, final Element sourceElement) throws MissingAttributeException {
        Attribute attribute = sourceElement.getAttribute(attributeName);
        if (attribute == null) {
            throw new MissingAttributeException(attributeName, sourceElement);
        }
        return attribute.getValue();
    }

    public static boolean parseBooleanAttributeValue(final String attributeName, final Element sourceElement) {
        try {
            return Boolean.parseBoolean(parseAttributeValue(attributeName, sourceElement));
        } catch (MissingAttributeException ex) {
            return false;
        }
    }

    public static int parseIntegerAttributeValue(final String attributeName, final Element sourceElement) throws XMLParsingException {
        try {
            return Integer.parseInt(parseAttributeValue(attributeName, sourceElement));
        } catch (NumberFormatException ex) {
            throw new XMLParsingException("Could not parse integer attribute[" + attributeName + "] for element[" + sourceElement.getQualifiedName() + "].", ex);
        }
    }

    public static <T extends Enum<T>> T parseEnumAttributeValue(final String attributeName, final Element sourceElement, final Class<T> enumType) throws XMLParsingException {
        String attributeValue = parseAttributeValue(attributeName, sourceElement);
        try {
            return Enum.valueOf(enumType, attributeValue);
        } catch (java.lang.IllegalArgumentException ex) {
            throw new XMLParsingException("Could not resolve enum value[" + attributeValue + "] out of attribute[" + attributeName + "] for element[" + sourceElement.getQualifiedName() + "].", ex);
        }
    }

    public static Elements parseChildElements(final Element sourceElement, final String childElementName, final boolean atLeastOne) throws XMLParsingException {
        Elements childElements = sourceElement.getChildElements(childElementName);
        if (atLeastOne && childElements.size() == 0) {
            throw new XMLParsingException("Missing at least one element[" + childElementName + "] for parent element[" + sourceElement.getQualifiedName() + "].");
        }
        return childElements;
    }

    public static String fixXML(final String xml) {
        return xml.replaceAll("zdef-?[^:]+:", ""); // [\\d]+:
    }
}
