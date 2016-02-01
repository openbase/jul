/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
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

import org.dc.jul.extension.rsb.iface.RSBListenerInterface;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import java.util.Iterator;
import java.util.List;
import rsb.Handler;
import rsb.Scope;
import rsb.filter.Filter;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class NotInitializedRSBListener extends NotInitializedRSBParticipant implements RSBListenerInterface {

    public NotInitializedRSBListener() {
    }

    public NotInitializedRSBListener(Scope scope) {
        super(scope);
    }

    @Override
    public List<Filter> getFilters() throws NotAvailableException {
        throw new NotAvailableException("filters", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public Iterator<Filter> getFilterIterator() throws NotAvailableException {
        throw new NotAvailableException("filter iterator", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public void addFilter(Filter filter) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not add filter!", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public List<Handler> getHandlers() throws NotAvailableException {
        throw new NotAvailableException("handlers", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public Iterator<Handler> getHandlerIterator() throws CouldNotPerformException {
        throw new NotAvailableException("handler iterator", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public void addHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException {
        throw new CouldNotPerformException("Could not add handler!", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public void removeHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException {
        throw new CouldNotPerformException("Could not remove handler!", new InvalidStateException("Listener not initialized!"));
    }
}
