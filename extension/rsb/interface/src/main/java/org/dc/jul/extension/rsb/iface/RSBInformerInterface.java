/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.iface;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Activatable;
import rsb.Event;
import rsb.RSBException;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine Threepwood
 * @param <DataType>
 */
public interface RSBInformerInterface<DataType extends Object> extends RSBParticipantInterface {

    /**
     * Send an {@link Event} to all subscribed participants.
     *
     * @param event the event to send
     * @return modified event with set timing information
     * @throws CouldNotPerformException error sending event
     */
    public Event send(final Event event) throws CouldNotPerformException;

    /**
     * Send data (of type T) to all subscribed participants.
     *
     * @param data data to send with default setting from the informer
     * @return generated event
     * @throws CouldNotPerformException
     */
    public Event send(final DataType data) throws CouldNotPerformException;

    /**
     * Returns the class describing the type of data sent by this informer.
     *
     * @return class
     */
    public Class<?> getTypeInfo() throws NotAvailableException;

    /**
     * Set the class object describing the type of data sent by this informer.
     *
     * @param typeInfo a {@link Class} instance describing the sent data
     */
    public void setTypeInfo(final Class<DataType> typeInfo) throws CouldNotPerformException;

}
