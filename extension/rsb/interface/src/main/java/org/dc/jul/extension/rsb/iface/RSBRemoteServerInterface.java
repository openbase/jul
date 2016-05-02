/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.iface;

/*
 * #%L
 * JUL Extension RSB Interface
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import java.util.concurrent.Future;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.TimeoutException;
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
