package org.openbase.jul.extension.rsb.com.strategy;

/*-
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import rsb.Event;
import rsb.Handler;
import rsb.eventprocessing.AbstractEventReceivingStrategy;
import rsb.filter.Filter;

/**
 * An {@link rsb.eventprocessing.EventReceivingStrategy} that uses a thread pool for all handlers.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ThreadPoolUnorderedEventReceivingStrategy
        extends AbstractEventReceivingStrategy {

    private Map<Future, DispatchTask> eventTaskMap;

    private final ExecutorService executorService;

    /**
     * A list of all registered filters for this {@link AbstractEventReceivingStrategy}.
     */
    private final Set<Filter> filters = Collections
            .synchronizedSet(new HashSet<Filter>());

    /**
     * A list of all registered handlers
     * for this {@link AbstractEventReceivingStrategy}.
     */
    private final Set<Handler> handlers = Collections
            .synchronizedSet(new HashSet<Handler>());

    /**
     * Create a new {@link ThreadPoolUnorderedEventReceivingStrategy}.
     *
     * @param executorService this ExecutorService is used to handle events
     */
    public ThreadPoolUnorderedEventReceivingStrategy(
            final ExecutorService executorService) {
        this.executorService = executorService;
    }

    @Override
    public void addFilter(final Filter filter) {
        this.filters.add(filter);
    }

    @Override
    public void removeFilter(final Filter filter) {
        this.filters.remove(filter);
    }

    @Override
    public void addHandler(final Handler handler, final boolean wait) {
        this.handlers.add(handler);
    }

    @Override
    public void removeHandler(final Handler handler, final boolean wait) {
        this.handlers.remove(handler);
    }

    /**
     * Returns a list of all registered filters
     * for this {@link AbstractEventReceivingStrategy}.
     * @return the filter list.
     */
    public Set<Filter> getFilters() {
        return filters;
    }

    /**
     * Returns a list of all registered handlers
     * for this {@link AbstractEventReceivingStrategy}.
     * @return the handler list.
     */
    public Set<Handler> getHandlers() {
        return handlers;
    }

    /**
     * A thread that matches events and dispatches them to all
     * handlers that are registered in his internal set of handlers.
     *
     *
     * @author jwienke
     */
    private class DispatchTask implements Callable<Void> {

        private final Event eventToDispatch;

        public DispatchTask(final Event eventToDispatch) {
            this.eventToDispatch = eventToDispatch;
        }

        @Override
        public Void call() {

            // match
            // TODO blocks filter potentially a long time
            synchronized (getFilters()) {
                // CHECKSTYLE.OFF: LineLength - no way to convince
                // eclipse to wrap this
                for (final Filter filter : getFilters()) {
                    if (!filter.match(eventToDispatch)) {
                        return null;
                    }
                }
                // CHECKSTYLE.ON: LineLength
            }

            // dispatch
            // TODO suboptimal locking. blocks handlers a very long time
            synchronized (getHandlers()) {
                // CHECKSTYLE.OFF: LineLength - no way to convince
                // eclipse to wrap this
                for (final Handler handler : getHandlers()) {
                    handler.internalNotify(eventToDispatch);
                }
                // CHECKSTYLE.ON: LineLength
            }
            return null;
        }

    }

    @Override
    public void handle(final Event event) {

        if (eventTaskMap == null) {
            // not active so event will be ignored.
            return;
        }

        final DispatchTask dispatchTask = new DispatchTask(event);
        final Future<Void> future = executorService.submit(dispatchTask);

        try {
        eventTaskMap.put(future, dispatchTask);
        } catch (NullPointerException ex) {
            // because we do not want to synchronize this method out
            // of performance reasons, the eventTaskMap can already be null again
            // if deactivate was called by another thread. In this case the
            // dispatched task need to be canceled as well.
            future.cancel(true);
        }
    }

    @Override
    public void activate() {
        synchronized (this) {
            if (this.eventTaskMap != null) {
                throw new IllegalStateException("Already activated.");
            }
            eventTaskMap = new HashMap<Future, DispatchTask>();
        }
    }

    @Override
    public void deactivate() throws InterruptedException {
        synchronized (this) {
            if (this.eventTaskMap == null) {
                throw new IllegalStateException("Already deactivated.");
            }
            for (final Future future : this.eventTaskMap.keySet()) {
                future.cancel(true);
            }
            this.eventTaskMap.clear();
            this.eventTaskMap = null;
        }
    }

    @Override
    public boolean isActive() {
        return this.eventTaskMap != null;
    }

}
