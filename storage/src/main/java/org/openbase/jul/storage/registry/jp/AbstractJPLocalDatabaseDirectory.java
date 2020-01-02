package org.openbase.jul.storage.registry.jp;

/*
 * #%L
 * JUL Storage
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.tools.FileHandler;
import org.openbase.jps.tools.FileHandler.AutoMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * <p>
 * This class is the base implementaion of a local database directory. Local means this directory can be reacted from scratch if needed.
 */
public abstract class AbstractJPLocalDatabaseDirectory extends AbstractJPDatabaseDirectory {

    public AbstractJPLocalDatabaseDirectory(final String[] commandIdentifier) {
        super(commandIdentifier);
        setAutoCreateMode(AutoMode.On);
    }

    @Override
    public void validate() throws JPValidationException {
        try {
            if (JPService.getProperty(JPResetDB.class).getValue()) {
                setAutoCreateMode(AutoMode.On);
                setExistenceHandling(FileHandler.ExistenceHandling.MustBeNew);
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }
        super.validate();
    }
}
