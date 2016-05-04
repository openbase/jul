package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
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

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.pattern.Remote;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rst.rsb.ScopeType;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 * @param <M>
 */
public interface RSBRemote<M extends GeneratedMessage> extends Remote<M>{

    public void init(final ScopeType.Scope scope) throws InitializationException, InterruptedException;
    
    public void init(final Scope scope) throws InitializationException, InterruptedException;

    public void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException;

    public void init(final ScopeType.Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException;

    public ScopeType.Scope getScope() throws NotAvailableException;
}
