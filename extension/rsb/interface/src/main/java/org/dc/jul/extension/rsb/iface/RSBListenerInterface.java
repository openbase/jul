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

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import java.util.Iterator;
import java.util.List;
import rsb.Handler;
import rsb.filter.Filter;

/**
 *
 * @author mpohling
 */
public interface RSBListenerInterface extends RSBParticipantInterface {

    public List<Filter> getFilters() throws NotAvailableException;

    public Iterator<Filter> getFilterIterator() throws NotAvailableException;

    public void addFilter(Filter filter) throws CouldNotPerformException;

    public List<Handler> getHandlers() throws NotAvailableException;

    public Iterator<Handler> getHandlerIterator() throws CouldNotPerformException;

    public void addHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException;

    public void removeHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException;
}
