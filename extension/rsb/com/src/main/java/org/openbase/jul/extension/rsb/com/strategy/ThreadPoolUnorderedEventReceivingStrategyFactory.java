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

import rsb.InitializeException;
import rsb.eventprocessing.EventReceivingStrategy;
import rsb.eventprocessing.EventReceivingStrategyFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * An {@link ThreadPoolUnorderedEventReceivingStrategyFactory}
 * for {@link ThreadPoolUnorderedEventReceivingStrategy} instances.
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ThreadPoolUnorderedEventReceivingStrategyFactory
        implements EventReceivingStrategyFactory {

    /**
     * The execution service used for the
     * {@link ThreadPoolUnorderedEventReceivingStrategy}.
     */
    private final ExecutorService executorService;

    /**
     * Default constructor using a new instance of
     * the java default CachedThreadPool executor.
     */
    public ThreadPoolUnorderedEventReceivingStrategyFactory() {
        this(Executors.newCachedThreadPool());
    }

    /**
     * Constructor creates a
     * new {@link ThreadPoolUnorderedEventReceivingStrategyFactory}.
     *
     * @param executorService the ExecutorService given to the
     *                        {@link ThreadPoolUnorderedEventReceivingStrategy}
     */
    public ThreadPoolUnorderedEventReceivingStrategyFactory(
            final ExecutorService executorService) {
        this.executorService = executorService;
    }

    /**
     * Create a new {@link ThreadPoolUnorderedEventReceivingStrategy}.
     *
     * @return the created {@link ThreadPoolUnorderedEventReceivingStrategy}
     *
     * @throws InitializeException if initialization fails
     */
    @Override
    public EventReceivingStrategy create() throws InitializeException {
        return new ThreadPoolUnorderedEventReceivingStrategy(executorService);
    }
}
