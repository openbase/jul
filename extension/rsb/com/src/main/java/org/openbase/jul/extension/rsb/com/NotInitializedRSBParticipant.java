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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import rsb.Factory;
import rsb.ParticipantId;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import org.openbase.jul.extension.rsb.iface.RSBParticipant;

/**
 *
 * * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public abstract class NotInitializedRSBParticipant implements RSBParticipant {

    private final Scope scope;

    public NotInitializedRSBParticipant() {
        this.scope = null;
    }

    public NotInitializedRSBParticipant(Scope scope) {
        this.scope = scope;
    }

    @Override
    public String getKind() throws NotAvailableException {
        throw new NotAvailableException("kind", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public Class<?> getDataType() throws NotAvailableException {
        throw new NotAvailableException("data type info", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public ParticipantId getId() throws NotAvailableException {
        throw new NotAvailableException("id", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public Scope getScope() throws NotAvailableException {
        if(scope == null) {
            throw new NotAvailableException("scope", new InvalidStateException("Participant not initialized!"));
        }
        return scope;
    }

    @Override
    public ParticipantConfig getConfig() throws NotAvailableException {
        throw new NotAvailableException("config", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public void setObserverManager(Factory.ParticipantObserverManager observerManager) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not register observer manager!", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        throw new CouldNotPerformException("Could not activate participant!", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        throw new CouldNotPerformException("Could not deactivate participant!", new InvalidStateException("Participant not initialized!"));
    }

    @Override
    public boolean isActive() {
        return false;
    }
}
