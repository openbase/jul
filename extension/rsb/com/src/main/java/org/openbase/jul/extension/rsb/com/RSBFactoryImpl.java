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

import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import org.openbase.jul.extension.rsb.iface.RSBFactory;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;

/**
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class RSBFactoryImpl implements RSBFactory {

    private static RSBFactoryImpl instance;

    private RSBFactoryImpl() {
    }

    public static synchronized RSBFactoryImpl getInstance() {
        if (instance == null) {
            instance = new RSBFactoryImpl();
        }
        return instance;
    }

    @Override
    public <DT> RSBInformer<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type) throws InstantiationException {
        return new RSBSynchronizedInformer<>(scope, type);
    }

    @Override
    public <DT> RSBInformer<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedInformer<>(scope, type);
    }

    @Override
    public <DT> RSBInformer<DT> createSynchronizedInformer(final String scope, final Class<DT> type) throws InstantiationException {
        try {
            return new RSBSynchronizedInformer<>(new Scope(scope), type);
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBInformer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public <DT> RSBInformer<DT> createSynchronizedInformer(final String scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException {
        try {
            return new RSBSynchronizedInformer<>(new Scope(scope), type);
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBInformer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBListener createSynchronizedListener(final Scope scope) throws InstantiationException {
        return new RSBSynchronizedListener(scope);
    }

    @Override
    public RSBListener createSynchronizedListener(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedListener(scope, config);
    }

    @Override
    public RSBListener createSynchronizedListener(final String scope) throws InstantiationException {
        try {
            return new RSBSynchronizedListener(new Scope(scope));
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBListener.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBListener createSynchronizedListener(final String scope, final ParticipantConfig config) throws InstantiationException {
        try {
            return new RSBSynchronizedListener(new Scope(scope), config);
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBListener.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBLocalServer createSynchronizedLocalServer(final Scope scope) throws InstantiationException {
        return new RSBSynchronizedLocalServer(scope);
    }

    @Override
    public RSBLocalServer createSynchronizedLocalServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedLocalServer(scope, config);
    }

    @Override
    public RSBLocalServer createSynchronizedLocalServer(final String scope) throws InstantiationException {
        try {
            return new RSBSynchronizedLocalServer(new Scope(scope));
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBLocalServer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBLocalServer createSynchronizedLocalServer(final String scope, final ParticipantConfig config) throws InstantiationException {
        try {
            return new RSBSynchronizedLocalServer(new Scope(scope), config);
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBLocalServer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBRemoteServer createSynchronizedRemoteServer(final Scope scope) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(scope);
    }

    @Override
    public RSBRemoteServer createSynchronizedRemoteServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(scope, config);
    }

    @Override
    public RSBRemoteServer createSynchronizedRemoteServer(final Scope scope, final Double timeout) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(scope, timeout);
    }

    @Override
    public RSBRemoteServer createSynchronizedRemoteServer(final String scope) throws InstantiationException {
        try {
            return new RSBSynchronizedRemoteServer(new Scope(scope));
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBRemoteServer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBRemoteServer createSynchronizedRemoteServer(final String scope, final ParticipantConfig config) throws InstantiationException {
        try {
            return new RSBSynchronizedRemoteServer(new Scope(scope), config);
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBRemoteServer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }

    @Override
    public RSBRemoteServer createSynchronizedRemoteServer(final String scope, final Double timeout) throws InstantiationException {
        try {
            return new RSBSynchronizedRemoteServer(new Scope(scope), timeout);
        } catch (IllegalArgumentException ex) {
            throw new InstantiationException(RSBRemoteServer.class, new InvalidStateException("Invalid Scope", ex));
        }
    }
}
