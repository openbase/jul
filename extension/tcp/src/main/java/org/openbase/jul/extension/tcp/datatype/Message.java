package org.openbase.jul.extension.tcp.datatype;

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

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import nu.xom.Attribute;
import nu.xom.Element;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class Message implements Serializable {

	public static final String QUESTION_KEY = "?";
	public static final String EXCLAMATION_KEY = "!";
	public static final String ELEMENT_MATCH = "Match";
	public static final String ELEMENT_WORD = "Word";
	public static final String ELEMENT_MESSAGE = "Message";
	public static final String ELEMENT_TIMESTEMP = "TimeStamp";
	public static final String ATTRIBUTE_TEXT = "text";

	private final List<String> machtes;
	private final String emitterName, receiverName;
	private final long timeStemp;

	@JsonCreator
	public Message() {
		this.machtes = null;
		this.emitterName = null;
		this.receiverName = null;
		this.timeStemp = -1;
	}

	public Message(final String text, final String senderName, final String receiverName) throws InstantiationException {
		this(senderName, receiverName);
		if(text.isEmpty()) {
			throw new InstantiationException(this, new NotAvailableException("text"));
		}
		this.machtes.add(text);
	}

	public Message(Collection<String> messages, String emitterName, String receiverName) {
		this(emitterName, receiverName);
		this.machtes.addAll(messages);
	}

	private Message(String emitterName, String receiverName) {
		this.timeStemp = System.currentTimeMillis();
		this.machtes = new ArrayList<String>();
		this.emitterName = emitterName;
		this.receiverName = receiverName;
	}

	@JsonIgnore
	public boolean isEmpty() {
		return machtes.isEmpty();
	}

	@JsonIgnore
	public String getFormattedBestMatch() {
		return "[" + timeStemp + "]" + emitterName + ": " + getBestMatch() + " (" + (machtes.size() - 1) + " more matches)";
	}

	/**
	 * @return the emitterName
	 */
	public String getEmitterName() {
		return emitterName;
	}

	/**
	 * @return the message
	 */
	public List<String> getMachtes() {
		return machtes;
	}

	/**
	 * @return the receiverName
	 */
	public String getReceiverName() {
		return receiverName;
	}

	/**
	 * @return the timeStemp
	 */
	public long getTimeStemp() {
		return timeStemp;
	}

	@ Override
	public String toString() {
		return getFormattedBestMatch();
	}

	@JsonIgnore
	public String getBestMatch() {
		return machtes.get(0);
	}

	@JsonIgnore
	public String getXPathForMatch(final String match) {
		return "//" + ELEMENT_MESSAGE + "/" + ELEMENT_MATCH + "[" + (machtes.indexOf(match) + 1) + "]/" + ELEMENT_WORD;
	}

	@JsonIgnore
	public static String getXPathForMatchID(final int matchID) {
		return "//" + ELEMENT_MESSAGE + "/" + ELEMENT_MATCH + "[" + (matchID + 1) + "]/" + ELEMENT_WORD;
	}

	@JsonIgnore
	public boolean isAnswerExpected() {
		return isExclamation() | isQuestion();
	}

	@JsonIgnore
	public boolean isExclamation() {
		return getBestMatch().endsWith(EXCLAMATION_KEY);
	}

	@JsonIgnore
	public boolean isQuestion() {
		return getBestMatch().endsWith(QUESTION_KEY);
	}

	public Element toElement() {
		Element messageElement = new Element(ELEMENT_MESSAGE);
		Element timestempElement = new Element(ELEMENT_TIMESTEMP);
		timestempElement.appendChild(Long.toString(getTimeStemp()));
		messageElement.appendChild(timestempElement);
		for (String match : machtes) {
			messageElement.appendChild(matchToElement(match));
		}
		return messageElement;
	}

	private Element matchToElement(final String match) {
		Element matchElement = new Element(ELEMENT_MATCH);
		matchElement.addAttribute(new Attribute(ATTRIBUTE_TEXT, match));
		Element wordElement;
		for (String word : match.trim().split(" ")) {
			wordElement = new Element(ELEMENT_WORD);
			wordElement.appendChild(word);
			matchElement.appendChild(wordElement);
		}
		return matchElement;
	}
}
