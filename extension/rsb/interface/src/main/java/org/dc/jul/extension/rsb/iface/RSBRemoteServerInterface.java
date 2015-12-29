/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.iface;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.TimeoutException;
import java.util.concurrent.Future;
import rsb.Event;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public interface RSBRemoteServerInterface extends RSBServerInterface {

    public double getTimeout() throws NotAvailableException;

    public Future<Event> callAsync(String name, Event event) throws CouldNotPerformException;

    public Future<Event> callAsync(String name) throws CouldNotPerformException;

    public <ReplyType extends Object, RequestType extends Object> Future<ReplyType> callAsync(String name, RequestType data) throws CouldNotPerformException;

    public Event call(String name, Event event) throws CouldNotPerformException, TimeoutException, InterruptedException;

    public Event call(String name, Event event, double timeout) throws CouldNotPerformException, TimeoutException, InterruptedException;

    public Event call(String name) throws CouldNotPerformException, TimeoutException, InterruptedException;

    public Event call(String name, double timeout) throws CouldNotPerformException, TimeoutException, InterruptedException;

    public <ReplyType extends Object, RequestType extends Object> ReplyType call(String name, RequestType data) throws CouldNotPerformException, TimeoutException, InterruptedException;

    public <ReplyType extends Object, RequestType extends Object> ReplyType call(String name, RequestType data, double timeout) throws CouldNotPerformException, TimeoutException, InterruptedException;

}
