/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBServerInterface;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
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