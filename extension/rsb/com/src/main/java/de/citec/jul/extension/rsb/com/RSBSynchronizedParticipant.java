/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBParticipantInterface;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.schedule.SyncObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Factory;
import rsb.InitializeException;
import rsb.Participant;
import rsb.ParticipantId;
import rsb.RSBException;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 * @param <P>
 */
public abstract class RSBSynchronizedParticipant<P extends Participant> implements RSBParticipantInterface {

    private final Logger logger = LoggerFactory.getLogger(RSBSynchronizedParticipant.class);

    private P participant;
    protected final SyncObject participantLock = new SyncObject("participant");
    protected final Scope scope;
    protected final ParticipantConfig config;

    protected RSBSynchronizedParticipant(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }
            this.scope = scope;
            this.config = config;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    abstract P init() throws InitializeException, InterruptedException;

    protected P getParticipant() throws NotAvailableException {
        if (participant == null) {
            throw new NotAvailableException("participant");
        }
        return participant;
    }

    @Override
    public ParticipantId getId() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getId();
            }
        } catch (Exception ex) {
            throw new NotAvailableException("id", ex);
        }
    }

    @Override
    public Scope getScope() throws NotAvailableException {
        return scope;
    }

    @Override
    public ParticipantConfig getConfig() throws NotAvailableException {
        if (config != null) {
            return config;
        }

        try {
            synchronized (participantLock) {
                return getParticipant().getConfig();
            }
        } catch (Exception ex) {
            throw new NotAvailableException("config", ex);
        }
    }

    @Override
    public void setObserverManager(final Factory.ParticipantObserverManager observerManager) throws CouldNotPerformException {
        try {
            synchronized (participantLock) {
                getParticipant().setObserverManager(observerManager);
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not regitser observer manager " + observerManager + "!", ex);
        }
    }

    @Override
    public String getKind() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getKind();
            }
        } catch (Exception ex) {
            throw new NotAvailableException("kind", ex);
        }
    }

    @Override
    public Class<?> getDataType() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getDataType();
            }
        } catch (Exception ex) {
            throw new NotAvailableException("data type", ex);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        try {
            synchronized (participantLock) {
                if (participant == null) {
                    participant = init();
                }
                logger.debug("participant[" + this.hashCode() + ":" + participant.isActive() + "] activate");
                if(participant.isActive()) {
                    logger.warn("Skip activation because Participant["+this.hashCode()+"] is already activated!");
                    return;
                }
                getParticipant().activate();
                logger.debug("participant[" + this.hashCode() + ":" + participant.isActive() + "] activated.");
            }
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not activate listener!", ex);
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        try {
            synchronized (participantLock) {

                if (participant == null) {
                    logger.warn("Ignore listener deactivation because listener was never activated!");
                    return;
                }

                logger.debug("participant[" + this.hashCode() + ":" + participant.isActive() + "] deactivate.");
                participant.deactivate();
                logger.debug("participant[" + this.hashCode() + ":" + participant.isActive() + "] deactivated.");
                participant = null;
            }
        } catch (RSBException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not deactivate listener!", ex);
        }
    }

    @Override
    public boolean isActive() {
        synchronized (participantLock) {
            if (participant == null) {
                return false;
            }
            return participant.isActive();
        }
    }

}
