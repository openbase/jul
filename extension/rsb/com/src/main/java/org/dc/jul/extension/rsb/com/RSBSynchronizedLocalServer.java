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
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.NotAvailableException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Factory;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.patterns.Callback;
import rsb.patterns.LocalServer;

/**
 *
 * @author mpohling
 */
public class RSBSynchronizedLocalServer extends RSBSynchronizedServer<LocalServer> implements RSBLocalServerInterface {

    protected final Logger logger = LoggerFactory.getLogger(RSBSynchronizedLocalServer.class);

    private final Map<String, Callback> localMethodStack;

    public RSBSynchronizedLocalServer(final Scope scope) throws InstantiationException {
        this(scope, null);
    }

    public RSBSynchronizedLocalServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        super(scope, config);
        this.localMethodStack = new HashMap<>();
    }

    @Override
    protected LocalServer init() throws InitializeException {
        try {
            synchronized (participantLock) {
                LocalServer localServer;
                if (config == null) {

                    localServer = Factory.getInstance().createLocalServer(scope);
                } else {
                    localServer = Factory.getInstance().createLocalServer(scope, config);

                }
                initMethods(localServer);
                return localServer;
            }
        } catch (CouldNotPerformException ex) {
            throw new InitializeException("Could not init local server!", ex);
        }
    }

    private void initMethods(final LocalServer localServer) throws NotAvailableException, CouldNotPerformException {
        MultiException.ExceptionStack exceptionStack = null;
        synchronized (participantLock) {
            for (Entry<String, Callback> entry : localMethodStack.entrySet()) {
                try {
                    localServer.addMethod(entry.getKey(), entry.getValue());
                } catch (RSBException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
            }
            try {
                MultiException.checkAndThrow("Could not register all methods!", exceptionStack);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, logger);
            }
        }
    }

    @Override
    public void addMethod(final String name, final Callback callback) throws CouldNotPerformException {
        try {
            if (name == null) {
                throw new NotAvailableException("name");
            }
            if (callback == null) {
                throw new NotAvailableException("callback");
            }
            localMethodStack.put(name, callback);
            try {
                synchronized (participantLock) {
                    getParticipant().addMethod(name, callback);
                }
            } catch (NotAvailableException ex) {
                logger.debug("Method[" + name + "] is cached and will be registered during init phase of local server.");
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not add Method[" + name + "]!", ex);
        }
    }

    @Override
    public void waitForShutdown() throws CouldNotPerformException, InterruptedException {
        try {
            synchronized (participantLock) {
                getParticipant().waitForShutdown();
            }
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not wait for shutdown!", ex);
        }
    }
}
