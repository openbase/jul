/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

import org.dc.jul.extension.rsb.iface.RSBListenerInterface;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Factory;
import rsb.Handler;
import rsb.InitializeException;
import rsb.Listener;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.filter.Filter;

/**
 *
 * @author mpohling
 */
public class RSBSynchronizedListener extends RSBSynchronizedParticipant<Listener> implements RSBListenerInterface {

    protected final Logger logger = LoggerFactory.getLogger(RSBSynchronizedListener.class);

    private List<Handler> handlerList;
    private List<Filter> filterList;

    protected RSBSynchronizedListener(final Scope scope) throws InstantiationException {
        this(scope, null);
    }

    protected RSBSynchronizedListener(final Scope scope, final ParticipantConfig config) throws InstantiationException {
        super(scope, config);
        this.handlerList = new ArrayList<>();
        this.filterList = new ArrayList<>();
    }

    @Override
    protected Listener init() throws InitializeException, InterruptedException {
        synchronized (participantLock) {
            Listener listener;
            if (config == null) {
                listener = Factory.getInstance().createListener(scope);
            } else {
                listener = Factory.getInstance().createListener(scope, config);
            }
            initFilters(listener);
            initHandlers(listener);
            return listener;
        }
    }

    private void initFilters(final Listener listener) {
        for (Filter filter : filterList) {
            listener.addFilter(filter);
        }
    }

    private void initHandlers(final Listener listener) throws InterruptedException {
        for (Handler handler : handlerList) {
            listener.addHandler(handler, true);
        }
    }

    @Override
    public List<Filter> getFilters() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getFilters();
            }
        } catch (Exception ex) {
            return Collections.unmodifiableList(filterList);
        }
    }

    @Override
    public Iterator<Filter> getFilterIterator() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getFilterIterator();
            }
        } catch (Exception ex) {
            return filterList.iterator();
        }
    }

    @Override
    public void addFilter(final Filter filter) throws CouldNotPerformException {
        try {
            if (filter == null) {
                throw new NotAvailableException("filter");
            }
            filterList.add(filter);
            try {
                synchronized (participantLock) {
                    getParticipant().addFilter(filter);
                }
            } catch (NotAvailableException ex) {
                logger.debug("Filter[" + filter + "] is cached and will be registered during init phrase of listener.");
            }
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not add filter " + filter + "!", ex);
        }
    }

    @Override
    public List<Handler> getHandlers() throws NotAvailableException {
        try {
            synchronized (participantLock) {
                return getParticipant().getHandlers();
            }
        } catch (Exception ex) {
            return Collections.unmodifiableList(handlerList);
        }
    }

    @Override
    public Iterator<Handler> getHandlerIterator() throws CouldNotPerformException {
        try {
            synchronized (participantLock) {
                return getParticipant().getHandlerIterator();
            }
        } catch (Exception ex) {
            return handlerList.iterator();
        }
    }

    @Override
    public void addHandler(final Handler handler, final boolean wait) throws InterruptedException, CouldNotPerformException {
        try {
            if (handler == null) {
                throw new NotAvailableException("handler");
            }
            handlerList.add(handler);
            try {
                synchronized (participantLock) {
                    getParticipant().addHandler(handler, wait);
                }
            } catch (NotAvailableException ex) {
                logger.debug("Handler[" + handler + "] is cached and will be registered during init phrase of listener.");
            }

        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not register handler " + handler + "!", ex);
        }
    }

    @Override
    public void removeHandler(final Handler handler, final boolean wait) throws InterruptedException, CouldNotPerformException {
        try {
            if (handler == null) {
                throw new NotAvailableException("handler");
            }
            handlerList.remove(handler);
            try {
                synchronized (participantLock) {
                    getParticipant().removeHandler(handler, wait);
                }
            } catch (NotAvailableException ex) {
                logger.debug("Handler[" + handler + "] removed out of cache.");
            }
        } catch (NotAvailableException ex) {
            throw new CouldNotPerformException("Could not remove handler " + handler + "!", ex);
        }
    }
}
