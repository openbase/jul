/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.iface;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import java.util.Iterator;
import java.util.List;
import rsb.Handler;
import rsb.filter.Filter;

/**
 *
 * @author mpohling
 */
public interface RSBListenerInterface extends RSBParticipantInterface {

    public List<Filter> getFilters() throws NotAvailableException;

    public Iterator<Filter> getFilterIterator() throws NotAvailableException;

    public void addFilter(Filter filter) throws CouldNotPerformException;

    public List<Handler> getHandlers() throws NotAvailableException;

    public Iterator<Handler> getHandlerIterator() throws CouldNotPerformException;

    public void addHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException;

    public void removeHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException;
}
