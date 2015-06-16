/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBListenerInterface;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import java.util.Iterator;
import java.util.List;
import rsb.Handler;
import rsb.Scope;
import rsb.filter.Filter;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class NotInitializedRSBListener extends NotInitializedRSBParticipant implements RSBListenerInterface {

    public NotInitializedRSBListener() {
    }

    public NotInitializedRSBListener(Scope scope) {
        super(scope);
    }

    @Override
    public List<Filter> getFilters() throws NotAvailableException {
        throw new NotAvailableException("filters", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public Iterator<Filter> getFilterIterator() throws NotAvailableException {
        throw new NotAvailableException("filter iterator", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public void addFilter(Filter filter) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not add filter!", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public List<Handler> getHandlers() throws NotAvailableException {
        throw new NotAvailableException("handlers", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public Iterator<Handler> getHandlerIterator() throws CouldNotPerformException {
        throw new NotAvailableException("handler iterator", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public void addHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException {
        throw new CouldNotPerformException("Could not add handler!", new InvalidStateException("Listener not initialized!"));
    }

    @Override
    public void removeHandler(Handler handler, boolean wait) throws InterruptedException, CouldNotPerformException {
        throw new CouldNotPerformException("Could not remove handler!", new InvalidStateException("Listener not initialized!"));
    }
}
