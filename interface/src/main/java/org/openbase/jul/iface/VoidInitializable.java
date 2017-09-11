package org.openbase.jul.iface;

/*-
 * #%L
 * JUL Interface
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InitializationException;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface VoidInitializable extends DefaultInitializableImpl<Void> {

    @Override
    public default void init(Void config) throws InitializationException, InterruptedException {
        // todo any better solution here?
        init();
    }

    @Override
    public default void init() throws InitializationException, InterruptedException {
        LoggerFactory.getLogger(VoidInitializable.class).warn("This Method has to be overwritten. Else using it is useless!");
        new FatalImplementationErrorException("Did not overwrite init()!", this);
    }

    @Override
    public default Void getDefaultConfig() {
        return null;
    }
}
