package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
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
import java.util.HashMap;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.pattern.Controller;

/**
 *
 * * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public class ControllerRegistry<KEY, ENTRY extends Controller & Identifiable<KEY>> extends RegistryImpl<KEY, ENTRY> {

    public ControllerRegistry() throws InstantiationException {
        super(new HashMap<>());
    }

    public ControllerRegistry(HashMap<KEY, ENTRY> entryMap) throws InstantiationException {
        super(entryMap);
    }

    @Override
    public void clear() throws CouldNotPerformException {
        MultiException.ExceptionStack exceptionStack = null;
        for (Controller controller : getEntries()) {
            try {
                controller.deactivate();
            } catch (InterruptedException ex) {
                ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
        }
        MultiException.checkAndThrow("Could not deactivate all devices!", exceptionStack);
        super.clear();
    }
}
