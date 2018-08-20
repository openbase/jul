package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import java.util.concurrent.Future;

import org.openbase.jul.extension.rsb.iface.RSBFuture;
import rsb.Event;
import rsb.Scope;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class NotInitializedRSBRemoteServer extends NotInitializedRSBServer implements RSBRemoteServer {

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
    public RSBFuture<Event> callAsync(String name, Event event) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public RSBFuture<Event> callAsync(String name) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not call Method["+name+"]!", new InvalidStateException("RemoteServer not initialized!"));
    }

    @Override
    public <ReplyType, RequestType> RSBFuture<ReplyType> callAsync(String name, RequestType data) throws CouldNotPerformException {
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
