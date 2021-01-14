package org.openbase.jul.storage.registry.jp;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPBoolean;
import org.openbase.jps.preset.JPReset;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPDeveloperMode extends AbstractJPBoolean {

    public final static String[] COMMAND_IDENTIFIERS = {"--dev"};

    public JPDeveloperMode() {
        super(COMMAND_IDENTIFIERS);
        registerDependingProperty(JPReset.class);
    }

    @Override
    protected Boolean getPropertyDefaultValue() {
        try {
            return JPService.getProperty(JPReset.class).getValue();
        } catch (JPNotAvailableException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access JPReset property", ex), logger);
            return false;
        }
    }

    @Override
    public String getDescription() {
        return "Flag can be used to activate the development mode of bco. This enables hardware independent tests and updates the registry to the latest developer version.";
    }
}
