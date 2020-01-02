package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
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
import org.openbase.jul.extension.rsb.iface.RSBListener;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RSBSynchronizedListener extends RSBSynchronizedParticipant<Listener> implements RSBListener {

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
