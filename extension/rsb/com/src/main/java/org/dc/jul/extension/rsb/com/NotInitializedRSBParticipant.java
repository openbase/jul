/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

import org.dc.jul.extension.rsb.iface.RSBParticipantInterface;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import rsb.Factory;
import rsb.ParticipantId;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public abstract class NotInitializedRSBParticipant implements RSBParticipantInterface {

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
