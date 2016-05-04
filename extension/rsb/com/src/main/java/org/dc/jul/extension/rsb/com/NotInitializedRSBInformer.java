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

import org.dc.jul.extension.rsb.iface.RSBInformerInterface;
import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import rsb.Event;
import rsb.Scope;

/**
 *
 * @author mpohling
 * @param <M>
 */
public class NotInitializedRSBInformer<M extends GeneratedMessage> extends NotInitializedRSBParticipant implements RSBInformerInterface<M> {

    public NotInitializedRSBInformer() {
    }

    public NotInitializedRSBInformer(Scope scope) {
        super(scope);
    }
    
    @Override
    public Event send(Event event) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could send event!", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public Event send(M data) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could send data!", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public Class<?> getTypeInfo() throws NotAvailableException {
        throw new NotAvailableException("type info", new InvalidStateException("Informer not initialized!"));
    }

    @Override
    public void setTypeInfo(Class<M> typeInfo) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not set type info!", new InvalidStateException("Informer not initialized!"));
    }
}
