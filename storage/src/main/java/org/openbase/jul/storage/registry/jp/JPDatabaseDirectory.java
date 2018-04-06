package org.openbase.jul.storage.registry.jp;

/*
 * #%L
 * JUL Storage
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
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.preset.JPShareDirectory;
import org.openbase.jps.preset.JPVarDirectory;
import org.openbase.jps.tools.FileHandler;

import java.io.File;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @deprecated This java property has been moved to the bco registry module.
 */
@Deprecated
public class JPDatabaseDirectory extends AbstractJPLocalDatabaseDirectory {

    public static final String DEFAULT_DB_PATH = "registry/db";

    public static final String[] COMMAND_IDENTIFIERS = {"--db", "--database"};

    public JPDatabaseDirectory() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    public File getParentDirectory() throws JPServiceException {
        try {
            if (new File(JPService.getProperty(JPVarDirectory.class).getValue(), DEFAULT_DB_PATH).exists() || JPService.testMode()) {
                return JPService.getProperty(JPVarDirectory.class).getValue();
            }
        } catch (JPNotAvailableException ex) {
            // continue with resolution via share folder...
        }

        try {
            if (new File(JPService.getProperty(JPShareDirectory.class).getValue(), DEFAULT_DB_PATH).exists()) {
                return JPService.getProperty(JPShareDirectory.class).getValue();
            }
        } catch (JPNotAvailableException ex) {
            // share could not be detected but exception is thrown anyway in next line so no report needed.
        }

        throw new JPServiceException("Could not detect db location!");
    }

    @Override
    protected File getPropertyDefaultValue() {
        return new File(DEFAULT_DB_PATH);
    }
    
    @Override
    public void validate() throws JPValidationException {
        if (JPService.testMode()) {
            setAutoCreateMode(FileHandler.AutoMode.On);
            setExistenceHandling(FileHandler.ExistenceHandling.Must);
        }
        super.validate();
    }
}
