package org.openbase.jul.extension.rsb.iface;

/*
 * #%L
 * JUL Extension RSB Interface
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import rsb.Event;

import java.util.concurrent.Future;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public interface RSBRemoteServer extends RSBServer {

    double getTimeout() throws NotAvailableException;

    Future<Event> callAsync(String name, Event event);

    Future<Event> callAsync(String name);

    <ReplyType extends Object, RequestType extends Object> Future<ReplyType> callAsync(String name, RequestType data);

    Event call(String name, Event event) throws CouldNotPerformException, InterruptedException;

    Event call(String name, Event event, double timeout) throws CouldNotPerformException, InterruptedException;

    Event call(String name) throws CouldNotPerformException, InterruptedException;

    Event call(String name, double timeout) throws CouldNotPerformException, InterruptedException;

    <ReplyType extends Object, RequestType extends Object> ReplyType call(String name, RequestType data) throws CouldNotPerformException, InterruptedException;

    <ReplyType extends Object, RequestType extends Object> ReplyType call(String name, RequestType data, double timeout) throws CouldNotPerformException, InterruptedException;

}
