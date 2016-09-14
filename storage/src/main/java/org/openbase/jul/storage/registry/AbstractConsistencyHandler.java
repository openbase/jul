package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.util.Map;
import org.openbase.jul.iface.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <VALUE>
 * @param <MAP>
 * @param <R>
 */
public abstract class AbstractConsistencyHandler<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends Registry<KEY, VALUE>> implements ConsistencyHandler<KEY, VALUE, MAP, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void reset() {
    }

    /**
     * Overwrite method to free resources or perform any special shutdown phrases.
     */
    @Override
    public void shutdown() {
        reset();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
