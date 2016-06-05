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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.extension.rsb.iface.RSBRemoteServerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Factory;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.patterns.RemoteServer;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class RSBSynchronizedRemoteServer extends RSBSynchronizedServer<RemoteServer> implements RSBRemoteServerInterface {

    protected final Logger logger = LoggerFactory.getLogger(RSBSynchronizedLocalServer.class);

    private final Double timeout;

    protected RSBSynchronizedRemoteServer(final Scope scope) throws InstantiationException {
        super(scope, null);
        this.timeout = null;
    }

    protected RSBSynchronizedRemoteServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        super(scope, config);
        this.timeout = null;
    }

    protected RSBSynchronizedRemoteServer(final Scope scope, final Double timeout) throws InstantiationException {
        super(scope, null);
        try {
            if (timeout == null) {
                throw new NotAvailableException("timeout");
            }
            this.timeout = timeout;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    protected RemoteServer init() throws InitializeException {
        synchronized (participantLock) {
            if (timeout == null && config == null) {
                return Factory.getInstance().createRemoteServer(scope);
            } else if (timeout == null) {
                return Factory.getInstance().createRemoteServer(scope, config);
            } else {
                return Factory.getInstance().createRemoteServer(scope, timeout);
            }
        }
    }

    @Override
    public double getTimeout() throws NotAvailableException {
        try {
            if (timeout != null) {
                return timeout;
            }
            synchronized (participantLock) {
                return getParticipant().getTimeout();
            }
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("timeout", ex);
        }
    }

    @Override
    public Future<Event> callAsync(final String name, final Event event) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().callAsync(name, event);
            }
        } catch (RSBException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex);
        }
    }

    @Override
    public Future<Event> callAsync(final String name) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().callAsync(name);
            }
        } catch (RSBException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex);
        }
    }

    @Override
    public <ReplyType, RequestType> Future<ReplyType> callAsync(final String name, final RequestType data) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().callAsync(name, data);
            }
        } catch (RSBException | CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex);
        }
    }

    @Override
    public Event call(final String name, final Event event) throws CouldNotPerformException, InterruptedException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            try {
                synchronized (participantLock) {
                    return getParticipant().call(name, event);
                }
            } catch (ExecutionException ex) {
                throw rsbInterruptedHack(ex);
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new org.dc.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    private Exception rsbInterruptedHack(Exception ex) throws InterruptedException {
        //rsb 0.12 hack
        Throwable cause = ex;
        while (cause != null) {
            if (cause instanceof InterruptedException) {
                throw (InterruptedException) cause;
            }
            cause = cause.getCause();
        }
        return ex;
    }

    @Override
    public Event call(final String name, final Event event, final double timeout) throws CouldNotPerformException, InterruptedException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            try {
                synchronized (participantLock) {
                    return getParticipant().call(name, event, timeout);
                }
            } catch (ExecutionException ex) {
                throw rsbInterruptedHack(ex);
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new org.dc.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public Event call(final String name) throws CouldNotPerformException, InterruptedException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            try {
                synchronized (participantLock) {
                    return getParticipant().call(name);
                }
            } catch (ExecutionException ex) {
                throw rsbInterruptedHack(ex);
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new org.dc.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public Event call(final String name, final double timeout) throws CouldNotPerformException, InterruptedException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            try {
                synchronized (participantLock) {
                    return getParticipant().call(name, timeout);
                }
            } catch (ExecutionException ex) {
                throw rsbInterruptedHack(ex);
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new org.dc.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public <ReplyType, RequestType> ReplyType call(final String name, final RequestType data) throws CouldNotPerformException, InterruptedException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            try {
                synchronized (participantLock) {
                    return getParticipant().call(name, data);
                }
            } catch (ExecutionException ex) {
                throw rsbInterruptedHack(ex);
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new org.dc.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public <ReplyType, RequestType> ReplyType call(final String name, final RequestType data, final double timeout) throws CouldNotPerformException, InterruptedException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            try {
                synchronized (participantLock) {
                    return getParticipant().call(name, data, timeout);
                }
            } catch (ExecutionException ex) {
                throw rsbInterruptedHack(ex);
            }
        } catch (InterruptedException ex) {
            throw ex;
        } catch (TimeoutException ex) {
            throw new org.dc.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }
}
