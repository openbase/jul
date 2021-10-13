package org.openbase.jul.communication.iface;

/*
 * #%L
 * JUL Communication Default
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.openbase.jul.communication.config.CommunicatorConfig;
import org.openbase.type.communication.ScopeType.Scope;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public interface CommunicatorFactory {

    <DT> Publisher<DT> createSynchronizedInformer(final org.openbase.type.communication.ScopeType.Scope scope, final Class<DT> type) throws InstantiationException;

    <DT> Publisher<DT> createSynchronizedInformer(final Scope scope, final Class<DT> type, final CommunicatorConfig config) throws InstantiationException;

    <DT> Publisher<DT> createSynchronizedInformer(final String scope, final Class<DT> type) throws InstantiationException;

    <DT> Publisher<DT> createSynchronizedInformer(final String scope, final Class<DT> type, final CommunicatorConfig config) throws InstantiationException;

    Subscriber createSynchronizedListener(final Scope scope) throws InstantiationException;

    Subscriber createSynchronizedListener(final Scope scope, final CommunicatorConfig config) throws InstantiationException;

    Subscriber createSynchronizedListener(final String scope) throws InstantiationException;

    Subscriber createSynchronizedListener(final String scope, final CommunicatorConfig config) throws InstantiationException;

    RPCServer createSynchronizedLocalServer(final Scope scope) throws InstantiationException;

    RPCServer createSynchronizedLocalServer(final Scope scope, final CommunicatorConfig config) throws InstantiationException;

    RPCServer createSynchronizedLocalServer(final String scope) throws InstantiationException;

    RPCServer createSynchronizedLocalServer(final String scope, final CommunicatorConfig config) throws InstantiationException;

    RPCClient createSynchronizedRemoteServer(final Scope scope) throws InstantiationException;

    RPCClient createSynchronizedRemoteServer(final Scope scope, final CommunicatorConfig config) throws InstantiationException;

    RPCClient createSynchronizedRemoteServer(final Scope scope, final Double timeout) throws InstantiationException;

    RPCClient createSynchronizedRemoteServer(final String scope) throws InstantiationException;

    RPCClient createSynchronizedRemoteServer(final String scope, final CommunicatorConfig config) throws InstantiationException;

    RPCClient createSynchronizedRemoteServer(final String scope, final Double timeout) throws InstantiationException;

}
