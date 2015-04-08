/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.VerificationFailedException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <KEY> ID Type
 * @param <M> Internal Message
 */
public class IdentifiableMessage<KEY, M extends GeneratedMessage> implements Identifiable<KEY> {

	protected final static Logger logger = LoggerFactory.getLogger(IdentifiableMessage.class);

	private M messageOrBuilder;
	private Observable<IdentifiableMessage<KEY, M>> observable;

	public IdentifiableMessage(M messageOrBuilder) {
		this.messageOrBuilder = messageOrBuilder;
		this.observable = new Observable<>();
	}

	@Override
	public KEY getId() throws CouldNotPerformException {
		try {
			if (messageOrBuilder == null) {
				throw new NotAvailableException("messageOrBuilder");
			}
			if (!messageOrBuilder.hasField(messageOrBuilder.getDescriptorForType().findFieldByName(FIELD_ID))) {
				throw new VerificationFailedException("Given message has no id value!");
			}
			KEY id = (KEY) messageOrBuilder.getField(messageOrBuilder.getDescriptorForType().findFieldByName(FIELD_ID));

			if(id.toString().isEmpty()) {
				throw new VerificationFailedException("Detected id is empty!");
			}

			return id;
			
		} catch (Exception ex) {
			throw new CouldNotPerformException("Could not detect id.");
		}
	}

	public void setMessage(final M message) throws CouldNotPerformException {
		if (message == null) {
			throw new NotAvailableException("message");
		}
		this.messageOrBuilder = message;
		try {
			observable.notifyObservers(this);
		} catch (MultiException ex) {
			ExceptionPrinter.printHistory(logger, ex);
		}
	}

	public M getMessage() {
		return messageOrBuilder;
	}

	@SuppressWarnings("unchecked")
	public void addObserver(Observer<? extends IdentifiableMessage<KEY, M>> observer) {
		observable.addObserver((Observer<IdentifiableMessage<KEY, M>>) observer);
	}

	@SuppressWarnings("unchecked")
	public void removeObserver(Observer<? extends IdentifiableMessage<KEY, M>> observer) {
		observable.removeObserver((Observer<IdentifiableMessage<KEY, M>>) observer);
	}

	@Override
	public String toString() {
		try {
			return getClass().getSimpleName() + "[" + getId().toString() + "]";
		} catch (CouldNotPerformException ex) {
			logger.warn("Could not return id value!", ex);
			return getClass().getSimpleName() + "[?]";
		}
	}
}
