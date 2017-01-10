package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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

import com.google.protobuf.GeneratedMessage;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.Remote;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rst.rsb.ScopeType;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @param <M>
 */
public interface RSBRemote<M extends GeneratedMessage> extends Remote<M>{

    public void init(final ScopeType.Scope scope) throws InitializationException, InterruptedException;
    
    public void init(final Scope scope) throws InitializationException, InterruptedException;

    public void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException;

    public void init(final ScopeType.Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException;

    /**
     * Method returns the scope of this remote connection.
     *
     * @return the remote controller scope.
     * @throws NotAvailableException
     */
    public ScopeType.Scope getScope() throws NotAvailableException;
}
