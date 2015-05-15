/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.iface;

import de.citec.jul.extension.rsb.iface.RSBLocalServerInterface;
import de.citec.jul.exception.InstantiationException;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 */
public interface RSBFactoryInterface {

    <DT> RSBInformerInterface<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type) throws InstantiationException;

    <DT> RSBInformerInterface<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException;

    <DT> RSBInformerInterface<DT> createSynchronizedInformer(final String scope, final Class<DT> type) throws InstantiationException;

    <DT> RSBInformerInterface<DT> createSynchronizedInformer(final String scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException;

    RSBListenerInterface createSynchronizedListener(final Scope scope) throws InstantiationException;

    RSBListenerInterface createSynchronizedListener(final Scope scope, final ParticipantConfig config) throws InstantiationException;

    RSBListenerInterface createSynchronizedListener(final String scope) throws InstantiationException;

    RSBListenerInterface createSynchronizedListener(final String scope, final ParticipantConfig config) throws InstantiationException;

    RSBLocalServerInterface createSynchronizedLocalServer(final Scope scope) throws InstantiationException;

    RSBLocalServerInterface createSynchronizedLocalServer(final Scope scope, final ParticipantConfig config) throws InstantiationException;

    RSBLocalServerInterface createSynchronizedLocalServer(final String scope) throws InstantiationException;

    RSBLocalServerInterface createSynchronizedLocalServer(final String scope, final ParticipantConfig config) throws InstantiationException;

    RSBRemoteServerInterface createSynchronizedRemoteServer(final Scope scope) throws InstantiationException;

    RSBRemoteServerInterface createSynchronizedRemoteServer(final Scope scope, final ParticipantConfig config) throws InstantiationException;

    RSBRemoteServerInterface createSynchronizedRemoteServer(final Scope scope, final Double timeout) throws InstantiationException;

    RSBRemoteServerInterface createSynchronizedRemoteServer(final String scope) throws InstantiationException;

    RSBRemoteServerInterface createSynchronizedRemoteServer(final String scope, final ParticipantConfig config) throws InstantiationException;

    RSBRemoteServerInterface createSynchronizedRemoteServer(final String scope, final Double timeout) throws InstantiationException;

}
