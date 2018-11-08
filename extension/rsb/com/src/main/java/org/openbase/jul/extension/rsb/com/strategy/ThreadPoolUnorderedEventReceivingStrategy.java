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
import java.util.concurrent.locks.ReentrantReadWriteLock;

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

    private final Map<Future, DispatchTask> eventTaskMap;

    private final ReentrantReadWriteLock activationLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock modificationLock = new ReentrantReadWriteLock();

    private final ExecutorService executorService;

    private volatile boolean active = false;

    /**
     * A list of all registered filters for this {@link AbstractEventReceivingStrategy}.
     */
    private final Set<Filter> filters = new HashSet<Filter>();

    /**
     * A list of all registered handlers
     * for this {@link AbstractEventReceivingStrategy}.
     */
    private final Set<Handler> handlers = new HashSet<Handler>();

    /**
     * Create a new {@link ThreadPoolUnorderedEventReceivingStrategy}.
     *
     * @param executorService this ExecutorService is used to handle events
     */
    public ThreadPoolUnorderedEventReceivingStrategy(
            final ExecutorService executorService) {
        this.executorService = executorService;
        this.eventTaskMap = new HashMap();
    }

    @Override
    public void addFilter(final Filter filter) {
        modificationLock.writeLock().lock();
        try {
            this.filters.add(filter);
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public void removeFilter(final Filter filter) {
        modificationLock.writeLock().lock();
        try {
            this.filters.remove(filter);
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public void addHandler(final Handler handler, final boolean wait) {
        modificationLock.writeLock().lock();
        try {
            this.handlers.add(handler);
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    @Override
    public void removeHandler(final Handler handler, final boolean wait) {
        modificationLock.writeLock().lock();
        try {
            this.handlers.remove(handler);
        } finally {
            modificationLock.writeLock().unlock();
        }
    }

    /**
     * Returns a list of all registered filters
     * for this {@link AbstractEventReceivingStrategy}.
     *
     * @return the filter list.
     */
    public Set<Filter> getFilters() {
        modificationLock.readLock().lock();
        try {
            return new HashSet<>(filters);
        } finally {
            modificationLock.readLock().unlock();
        }
    }

    /**
     * Returns a list of all registered handlers
     * for this {@link AbstractEventReceivingStrategy}.
     *
     * @return the handler list.
     */
    public Set<Handler> getHandlers() {
        modificationLock.readLock().lock();
        try {
            return new HashSet<>(handlers);
        } finally {
            modificationLock.readLock().unlock();
        }
    }

    /**
     * A thread that matches events and dispatches them to all
     * handlers that are registered in his internal set of handlers.
     *
     * @author divine
     */
    private class DispatchTask implements Callable<Void> {

        private final Event eventToDispatch;

        public DispatchTask(final Event eventToDispatch) {
            this.eventToDispatch = eventToDispatch;
        }

        @Override
        public Void call() {

            modificationLock.readLock().lock();
            try {
                // match
                for (final Filter filter : getFilters()) {
                    if (!filter.match(eventToDispatch)) {
                        return null;
                    }
                }

                // dispatch
                for (final Handler handler : getHandlers()) {
                    handler.internalNotify(eventToDispatch);
                }
            } finally {
                modificationLock.readLock().unlock();
            }
            return null;
        }
    }

    @Override
    public void handle(final Event event) {

        activationLock.readLock().lock();
        try {

            if (!active) {
                // not active so event will be ignored.
                return;
            }

            final DispatchTask dispatchTask = new DispatchTask(event);
            final Future<Void> future = executorService.submit(dispatchTask);

              eventTaskMap.put(future, dispatchTask);
        } finally {
            activationLock.readLock().unlock();
        }
    }

    @Override
    public void activate() {
        activationLock.writeLock().lock();
        try {
            if (active) {
                throw new IllegalStateException("Already activated.");
            }
            active = true;
        } finally {
            activationLock.writeLock().unlock();
        }
    }

    @Override
    public void deactivate() throws InterruptedException {
        activationLock.writeLock().lock();
        try {
            if (!active) {
                throw new IllegalStateException("Already deactivated.");
            }
            active = false;

            for (final Future future : this.eventTaskMap.keySet()) {
                future.cancel(true);
            }
            this.eventTaskMap.clear();

        } finally {
            activationLock.writeLock().unlock();
        }
    }

    @Override
    public boolean isActive() {
        activationLock.readLock().lock();
        try {
            return active;
        } finally {
            activationLock.readLock().unlock();
        }
    }
}
