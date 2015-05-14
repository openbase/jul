/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Activatable;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Filter;
import rsb.Factory;
import rsb.Handler;
import rsb.ParticipantId;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author mpohling
 */
public interface RSBListenerInterface extends Activatable {

    public ParticipantId getId() throws NotAvailableException;

    public Scope getScope() throws NotAvailableException;

    public ParticipantConfig getConfig() throws NotAvailableException;

    public void setObserverManager(Factory.ParticipantObserverManager observerManager) throws CouldNotPerformException;

    public List<Filter> getFilters() throws NotAvailableException;

    public Iterator<Filter> getFilterIterator() throws NotAvailableException;

    public void addFilter(Filter filter) throws CouldNotPerformException;

    public List<Handler> getHandlers() throws NotAvailableException;

    public Iterator<Handler> getHandlerIterator() throws CouldNotPerformException;

    public void addHandler(Handler handler, boolean wait) throws InterruptedException;

    public void removeHandler(Handler handler, boolean wait) throws InterruptedException;

    public String getKind() throws NotAvailableException;

    public Class<?> getDataType() throws NotAvailableException;
}
