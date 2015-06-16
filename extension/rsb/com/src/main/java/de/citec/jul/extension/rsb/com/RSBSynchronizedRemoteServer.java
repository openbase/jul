/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBRemoteServerInterface;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeoutException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Factory;
import rsb.InitializeException;
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
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
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "] asynchronous!", ex);
        }
    }

    @Override
    public Event call(final String name, final Event event) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().call(name, event);
            }
        } catch (TimeoutException ex) {
            throw new de.citec.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public Event call(final String name, final Event event, final double timeout) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().call(name, event, timeout);
            }
        } catch (TimeoutException ex) {
            throw new de.citec.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public Event call(final String name) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().call(name);
            }
        } catch (TimeoutException ex) {
            throw new de.citec.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public Event call(final String name, final double timeout) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().call(name, timeout);
            }
        } catch (TimeoutException ex) {
            throw new de.citec.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public <ReplyType, RequestType> ReplyType call(final String name, final RequestType data) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().call(name, data);
            }
        } catch (TimeoutException ex) {
            throw new de.citec.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }

    @Override
    public <ReplyType, RequestType> ReplyType call(final String name, final RequestType data, final double timeout) throws CouldNotPerformException {
        try {
            if (name == null || name.isEmpty()) {
                throw new NotAvailableException("name");
            }
            synchronized (participantLock) {
                return getParticipant().call(name, data, timeout);
            }
        } catch (TimeoutException ex) {
            throw new de.citec.jul.exception.TimeoutException("Could not call Method[" + name + "] in time!", ex);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not call Method[" + name + "]!", ex);
        }
    }
}
