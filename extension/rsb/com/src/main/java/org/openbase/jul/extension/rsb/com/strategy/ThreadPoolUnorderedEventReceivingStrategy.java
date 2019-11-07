package org.openbase.jul.extension.rsb.com.strategy;

/*-
 * #%L
 * JUL Extension RSB Communication
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.Map;
import java.util.concurrent.*;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.schedule.RecurrenceEventFilter;
import org.openbase.jul.schedule.SyncObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
public class ThreadPoolUnorderedEventReceivingStrategy extends AbstractEventReceivingStrategy {

    public static final int MAX_PARALLEL_DISPATCH_TASK_COUNT = 10;

    private static Logger LOGGER = LoggerFactory.getLogger(ThreadPoolUnorderedEventReceivingStrategy.class);

    private final Map<DispatchTask, Future> eventTaskMap;

    private final ReentrantReadWriteLock activationLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock modificationLock = new ReentrantReadWriteLock();

    private final ExecutorService executorService;

    private final ArrayList<Event> eventCache = new ArrayList<>();
    private final SyncObject eventCacheLock = new SyncObject("EventCacheLock");

    private volatile boolean active = false;

    /**
     * A list of all registered filters for this {@link AbstractEventReceivingStrategy}.
     */
    private final Set<Filter> filters = new HashSet<>();

    /**
     * A list of all registered handlers
     * for this {@link AbstractEventReceivingStrategy}.
     */
    private final Set<Handler> handlers = new HashSet<>();

    private static final RecurrenceEventFilter<String> logEventFilter = new RecurrenceEventFilter<String>(3000) {
        @Override
        public void relay() throws Exception {
            LOGGER.warn(getLatestValue());
        }
    };

    /**
     * Create a new {@link ThreadPoolUnorderedEventReceivingStrategy}.
     *
     * @param executorService this ExecutorService is used to handle events
     */
    public ThreadPoolUnorderedEventReceivingStrategy(
            final ExecutorService executorService) {
        this.executorService = executorService;
        this.eventTaskMap = new ConcurrentHashMap<>();
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

                    // skip if task was interrupted.
                    if (Thread.interrupted()) {
                        return null;
                    }
                    // notify handler about new task
                    try {
                        handler.internalNotify(eventToDispatch);
                    } catch (InterruptedException e) {
                        return null;
                    }
                }
            } finally {

                // unlock modification lock
                modificationLock.readLock().unlock();

                // deregister task
                eventTaskMap.remove(this);

                // if events are cached then we execute the next one.
                synchronized (eventCacheLock) {
                    if (!eventCache.isEmpty()) {
                        handle(eventCache.remove(0));
                    }
                }
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

            // in case we are handling to many events, we cache them for later handling.
            synchronized (eventCacheLock) {
                if (eventTaskMap.size() > MAX_PARALLEL_DISPATCH_TASK_COUNT) {

                    // cache event
                    eventCache.add(event);

                    // print warnings if required
                    try {
                        final int taskCounter = eventTaskMap.size() + eventCache.size();
                        if (taskCounter > 50) {
                            logEventFilter.trigger("Participant[" + event.getScope() + (event.getMethod() != null ? "/" + event.getMethod() : "") + "] overload detected! Processing " + taskCounter + " tasks at once probably affects the application performance.");
                        } else {
                            if(JPService.verboseMode()) {
                                logEventFilter.trigger("Cache incoming event of Participant[" + event.getScope() + (event.getMethod() != null ? "/" + event.getMethod() : "") + "] for later execution, current cache size: " + eventTaskMap.size());
                            }
                        }
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(ex, LOGGER);
                    }

                    return;
                }
            }

            // dispatch new task
            try {
                final DispatchTask dispatchTask = new DispatchTask(event);
                final Future<Void> future = executorService.submit(dispatchTask);
                eventTaskMap.put(dispatchTask, future);

                // handle if execution was faster than registration
                if (future.isDone() && eventTaskMap.containsKey(dispatchTask)) {
                    eventTaskMap.remove(dispatchTask);
                }
            } catch (RejectedExecutionException ex) {
                ExceptionPrinter.printHistory("Event[" + event.toString() + "] execution rejected! System is probably shutting down or executor service overload occurred.", ex, LOGGER, LogLevel.WARN);
            }

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

            for (final Future future : this.eventTaskMap.values()) {
                if (!future.isDone()) {
                    future.cancel(true);
                }
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
