package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

import org.openbase.jul.extension.rsb.iface.RSBFactoryInterface;
import org.openbase.jul.extension.rsb.iface.RSBListenerInterface;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServerInterface;
import org.openbase.jul.extension.rsb.iface.RSBInformerInterface;
import org.openbase.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.openbase.jul.exception.InstantiationException;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class RSBFactory implements RSBFactoryInterface {

    private static RSBFactory instance;

    private RSBFactory() {
    }

    public static synchronized RSBFactory getInstance() {
        if (instance == null) {
            instance = new RSBFactory();
        }
        return instance;
    }

    @Override
    public <DT> RSBInformerInterface<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type) throws InstantiationException {
        return new RSBSynchronizedInformer<>(scope, type);
    }

    @Override
    public <DT> RSBInformerInterface<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedInformer<>(scope, type);
    }

    @Override
    public <DT> RSBInformerInterface<DT> createSynchronizedInformer(final String scope, final Class<DT> type) throws InstantiationException {
        return new RSBSynchronizedInformer<>(new Scope(scope), type);
    }

    @Override
    public <DT> RSBInformerInterface<DT> createSynchronizedInformer(final String scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedInformer<>(new Scope(scope), type);
    }

    @Override
    public RSBListenerInterface createSynchronizedListener(final Scope scope) throws InstantiationException {
        return new RSBSynchronizedListener(scope);
    }

    @Override
    public RSBListenerInterface createSynchronizedListener(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedListener(scope, config);
    }

    @Override
    public RSBListenerInterface createSynchronizedListener(final String scope) throws InstantiationException {
        return new RSBSynchronizedListener(new Scope(scope));
    }

    @Override
    public RSBListenerInterface createSynchronizedListener(final String scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedListener(new Scope(scope), config);
    }

    @Override
    public RSBLocalServerInterface createSynchronizedLocalServer(final Scope scope) throws InstantiationException {
        return new RSBSynchronizedLocalServer(scope);
    }

    @Override
    public RSBLocalServerInterface createSynchronizedLocalServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedLocalServer(scope, config);
    }

    @Override
    public RSBLocalServerInterface createSynchronizedLocalServer(final String scope) throws InstantiationException {
        return new RSBSynchronizedLocalServer(new Scope(scope));
    }

    @Override
    public RSBLocalServerInterface createSynchronizedLocalServer(final String scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedLocalServer(new Scope(scope), config);
    }

    @Override
    public RSBRemoteServerInterface createSynchronizedRemoteServer(final Scope scope) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(scope);
    }

    @Override
    public RSBRemoteServerInterface createSynchronizedRemoteServer(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(scope, config);
    }

    @Override
    public RSBRemoteServerInterface createSynchronizedRemoteServer(final Scope scope, final Double timeout) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(scope, timeout);
    }

    @Override
    public RSBRemoteServerInterface createSynchronizedRemoteServer(final String scope) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(new Scope(scope));
    }

    @Override
    public RSBRemoteServerInterface createSynchronizedRemoteServer(final String scope, final ParticipantConfig config) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(new Scope(scope), config);
    }

    @Override
    public RSBRemoteServerInterface createSynchronizedRemoteServer(final String scope, final Double timeout) throws InstantiationException {
        return new RSBSynchronizedRemoteServer(new Scope(scope), timeout);
    }
}
