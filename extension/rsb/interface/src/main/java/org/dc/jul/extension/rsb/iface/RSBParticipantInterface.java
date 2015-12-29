/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.iface;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Activatable;
import rsb.Factory;
import rsb.ParticipantId;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author mpohling
 */
public interface RSBParticipantInterface extends Activatable {

    public String getKind() throws NotAvailableException;

    public Class<?> getDataType() throws NotAvailableException;

    public ParticipantId getId() throws NotAvailableException;

    public Scope getScope() throws NotAvailableException;

    public ParticipantConfig getConfig() throws NotAvailableException;

    public void setObserverManager(Factory.ParticipantObserverManager observerManager) throws CouldNotPerformException;

}
