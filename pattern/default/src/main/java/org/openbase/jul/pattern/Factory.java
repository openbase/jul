package org.openbase.jul.pattern;

/*
 * #%L
 * JUL Pattern Default
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
import org.openbase.jul.exception.InstantiationException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * Factory pattern interface.
 *
 * @param <INSTANCE> Type of instance which can be created by using this factory.
 * @param <CONFIG> Configuration type which contains all attributes to create a new instance.
 */
public interface Factory<INSTANCE, CONFIG> {

    /**
     * Creates a new instance with the given configuration.
     *
     * @param config the configuration for the instance
     * @return the created instance
     * @throws org.openbase.jul.exception.InstantiationException if the initialization fails
     * @throws java.lang.InterruptedException if the initialization is interrupted
     */
    INSTANCE newInstance(final CONFIG config) throws InstantiationException, InterruptedException;
}
