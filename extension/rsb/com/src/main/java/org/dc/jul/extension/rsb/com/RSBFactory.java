/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

import org.dc.jul.extension.rsb.iface.RSBFactoryInterface;
import org.dc.jul.extension.rsb.iface.RSBListenerInterface;
import org.dc.jul.extension.rsb.iface.RSBRemoteServerInterface;
import org.dc.jul.extension.rsb.iface.RSBInformerInterface;
import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.dc.jul.exception.InstantiationException;
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
