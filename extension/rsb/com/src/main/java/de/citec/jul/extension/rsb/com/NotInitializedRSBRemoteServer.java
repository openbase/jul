/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBRemoteServerInterface;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import java.util.concurrent.Future;
import rsb.Event;
import rsb.Scope;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public class NotInitializedRSBRemoteServer extends NotInitializedRSBServer implements RSBRemoteServerInterface {

    public NotInitializedRSBRemoteServer() {
    }

    public NotInitializedRSBRemoteServer(Scope scope) {
        super(scope);
    }

    @Override
    public double getTimeout() throws NotAvailableException {
        throw new NotAvailableException("timeout", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public Future<Event> callAsync(String name, Event event) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public Future<Event> callAsync(String name) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public <ReplyType, RequestType> Future<ReplyType> callAsync(String name, RequestType data) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public Event call(String name, Event event) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public Event call(String name, Event event, double timeout) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public Event call(String name) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public Event call(String name, double timeout) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public <ReplyType, RequestType> ReplyType call(String name, RequestType data) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public <ReplyType, RequestType> ReplyType call(String name, RequestType data, double timeout) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }
}
