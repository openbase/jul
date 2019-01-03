package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.rsb.com.exception.RSBResolvedException;
import org.openbase.jul.extension.rsb.iface.RSBFuture;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;
import org.openbase.jul.schedule.FutureProcessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.*;
import rsb.config.ParticipantConfig;
import rsb.patterns.RemoteServer;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class RSBSynchronizedRemoteServer extends RSBSynchronizedServer<RemoteServer> implements RSBRemoteServer {

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
    public RSBFuture<Event> callAsync(final String name, final Event event) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                try {
                    return new RSBFutureImpl<>(getParticipant().callAsync(name, event));
                } catch (RSBException ex) {
                    throw new RSBResolvedException("Remote call failed!", ex);
                }
            }
        } catch (CouldNotPerformException ex) {
            if (Thread.currentThread().isInterrupted()) {
                return new RSBFutureImpl<>(FutureProcessor.canceledFuture(Event.class, ex));
            }
            return new RSBFutureImpl<>(FutureProcessor.canceledFuture(Event.class, new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex)));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
        }
    }

    @Override
    public RSBFuture<Event> callAsync(final String name) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                try {
                    return new RSBFutureImpl<>(getParticipant().callAsync(name));
                } catch (RSBException ex) {
                    throw new RSBResolvedException("Remote call failed!", ex);
                }
            }
        } catch (CouldNotPerformException ex) {
            if (Thread.currentThread().isInterrupted()) {
                return new RSBFutureImpl<>(FutureProcessor.canceledFuture(Event.class, ex));
            }
            return new RSBFutureImpl<>(FutureProcessor.canceledFuture(Event.class, new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex)));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
        }
    }

    @Override
    public RSBFuture<Object> callAsync(final String name, final Object data) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                try {
                    return new RSBFutureImpl<>(getParticipant().callAsync(name, data));
                } catch (RSBException ex) {
                    throw new RSBResolvedException("Remote call failed!", ex);
                }
            }
        } catch (CouldNotPerformException ex) {
            if (Thread.currentThread().isInterrupted()) {
                return new RSBFutureImpl<>(FutureProcessor.canceledFuture(Object.class, ex));
            }
            return new RSBFutureImpl<>(FutureProcessor.canceledFuture(Object.class, new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex)));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
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
            throw new org.openbase.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (RSBException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", new RSBResolvedException("Remote call failed!", ex));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
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
            throw new org.openbase.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (RSBException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", new RSBResolvedException("Remote call failed!", ex));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
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
            throw new org.openbase.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (RSBException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", new RSBResolvedException("Remote call failed!", ex));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
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
            throw new org.openbase.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (RSBException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", new RSBResolvedException("Remote call failed!", ex));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
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
            throw new org.openbase.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (RSBException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", new RSBResolvedException("Remote call failed!", ex));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
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
            throw new org.openbase.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (RSBException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", new RSBResolvedException("Remote call failed!", ex));
        } catch (RuntimeException ex) {
            throw new InvalidStateException("Could not call Method[" + name + "] asynchronous because of a middleware issue!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }
}
