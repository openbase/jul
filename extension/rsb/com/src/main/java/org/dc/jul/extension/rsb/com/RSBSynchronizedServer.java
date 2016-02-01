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

import org.dc.jul.extension.rsb.iface.RSBServerInterface;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.patterns.Method;
import rsb.patterns.Server;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 * @param <P>
 */
public abstract class RSBSynchronizedServer<P extends Server> extends RSBSynchronizedParticipant<P> implements RSBServerInterface {

    private final Logger logger = LoggerFactory.getLogger(RSBSynchronizedServer.class);

    public RSBSynchronizedServer(final Scope scope) throws InstantiationException {
        super(scope, null);
    }
    public RSBSynchronizedServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        super(scope, config);
    }

    @Override
    public Collection<? extends Method> getMethods() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getMethods();
            }
        } catch (Exception ex) {
            throw new NotAvailableException("methods", ex);
        }
    }

    @Override
    public Method getMethod(final String name) throws NotAvailableException {
        try {
            if (name == null) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().getMethod(name);
            }
        } catch (Exception ex) {
            throw new NotAvailableException("Method[" + name + "]", ex);
        }
    }

    @Override
    public boolean hasMethod(final String name) {
        try {
            if (name == null) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().hasMethod(name);
            }
        } catch (Exception ex) {
            return false;
        }
    }
}