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

import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import rsb.Scope;
import rsb.patterns.Callback;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class NotInitializedRSBLocalServer extends NotInitializedRSBServer implements RSBLocalServerInterface {

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
