package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import rsb.Scope;
import rsb.patterns.Callback;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class NotInitializedRSBLocalServer extends NotInitializedRSBServer implements RSBLocalServer {

    public NotInitializedRSBLocalServer() {
    }

    public NotInitializedRSBLocalServer(Scope scope) {
        super(scope);
    }

    @Override
    public void addMethod(String name, Callback callback) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not add Method["+name+"]!", new InvalidStateException("LocalServer not initialized!"));
    }

    @Override
    public void waitForShutdown() throws CouldNotPerformException, InterruptedException {
        return;
    }
}
