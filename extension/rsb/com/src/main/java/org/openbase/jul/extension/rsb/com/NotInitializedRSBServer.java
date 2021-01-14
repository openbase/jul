package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import java.util.Collection;
import rsb.Scope;
import rsb.patterns.Method;
import org.openbase.jul.extension.rsb.iface.RSBServer;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public abstract class NotInitializedRSBServer extends NotInitializedRSBParticipant implements RSBServer {

    public NotInitializedRSBServer() {
    }

    public NotInitializedRSBServer(Scope scope) {
        super(scope);
    }

    @Override
    public Collection<? extends Method> getMethods() throws NotAvailableException {
        throw new NotAvailableException("methods", new InvalidStateException("Server not initialized!"));
    }

    @Override
    public Method getMethod(String name) throws NotAvailableException {
        throw new NotAvailableException("Method["+name+"]", new InvalidStateException("Server not initialized!"));
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

}
