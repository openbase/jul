/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.iface;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.TimeoutException;
import rsb.Event;
import rsb.patterns.Future;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public interface RSBRemoteServerInterface extends RSBServerInterface {

    public double getTimeout() throws NotAvailableException;

    public Future<Event> callAsync(String name, Event event) throws CouldNotPerformException;

    public Future<Event> callAsync(String name) throws CouldNotPerformException;

    public <ReplyType extends Object, RequestType extends Object> Future<ReplyType> callAsync(String name, RequestType data) throws CouldNotPerformException;

    public Event call(String name, Event event) throws CouldNotPerformException, TimeoutException;

    public Event call(String name, Event event, double timeout) throws CouldNotPerformException, TimeoutException;

    public Event call(String name) throws CouldNotPerformException, TimeoutException;

    public Event call(String name, double timeout) throws CouldNotPerformException, TimeoutException;

    public <ReplyType extends Object, RequestType extends Object> ReplyType call(String name, RequestType data) throws CouldNotPerformException, TimeoutException;

    public <ReplyType extends Object, RequestType extends Object> ReplyType call(String name, RequestType data, double timeout) throws CouldNotPerformException, TimeoutException;

}
