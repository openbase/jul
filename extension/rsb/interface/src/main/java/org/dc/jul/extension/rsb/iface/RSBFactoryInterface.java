package org.dc.jul.extension.rsb.iface;

/*
 * #%L
 * JUL Extension RSB Interface
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
import org.dc.jul.exception.InstantiationException;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
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
