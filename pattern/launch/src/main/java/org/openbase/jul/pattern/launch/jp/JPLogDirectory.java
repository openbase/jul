package org.openbase.jul.pattern.launch.jp;

/*-
 * #%L
 * JUL Pattern Launch
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
import org.openbase.jps.exception.JPNotAvailableException;
import org.openbase.jps.preset.AbstractJPDirectory;
import org.openbase.jps.tools.FileHandler.AutoMode;
import org.openbase.jps.tools.FileHandler.ExistenceHandling;
import org.openbase.jul.processing.StringProcessor;

import java.io.File;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPLogDirectory extends AbstractJPDirectory {

    public final static String[] COMMAND_IDENTIFIERS = {"--log-dir"};

    public JPLogDirectory() {
        super(COMMAND_IDENTIFIERS, ExistenceHandling.CanExist, AutoMode.Off);
    }

    @Override
    protected File getPropertyDefaultValue() throws JPNotAvailableException {
        return new File("/var/log/" + StringProcessor.transformToNormalizedFileName(JPService.getApplicationName()));
    }

    @Override
    public String getDescription() {
        return "Can be used to refer the directory where the log files should be stored. This value is overrideable via the logger config file. ";
    }
}
