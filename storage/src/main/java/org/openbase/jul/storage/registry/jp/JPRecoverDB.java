package org.openbase.jul.storage.registry.jp;

/*
 * #%L
 * JUL Storage
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

import org.openbase.jps.core.AbstractJavaProperty.ValueType;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPBadArgumentException;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.exception.JPValidationException;
import org.openbase.jps.preset.AbstractJPBoolean;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class JPRecoverDB extends AbstractJPBoolean {

    public final static String[] COMMAND_IDENTIFIERS = {"--recover"};

    public JPRecoverDB() {
        super(COMMAND_IDENTIFIERS);
    }

    @Override
    protected Boolean getPropertyDefaultValue() {
        return false;
    }

    @Override
    public void validate() throws JPValidationException {
        super.validate();
        if (getValueType().equals((ValueType.CommandLine))) {
            logger.warn("WARNING: RECOVER CURRENT DATABASE!!!");
            logger.warn("WARNING: Entries which can not be recovered will be modifiered or erased to ensure db consistency.");
            try {
                if (JPService.getProperty(JPTestMode.class).getValue()) {
                    return;
                }
            } catch (JPServiceException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            }

            logger.warn("=== Type y and press enter to contine ===");
            try {
                if (!(System.in.read() == 'y')) {
                    throw new JPValidationException("Execution aborted by user!");
                }
            } catch (IOException ex) {
                throw new JPValidationException("Validation failed because of invalid input state!", ex);
            }
        }
    }

    @Override
    protected Boolean parse(List<String> arguments) throws JPBadArgumentException {
        return super.parse(arguments);
    }

    @Override
    public String getDescription() {
        return "Enables the db recovery mode to establish consistency by recovering broken database entries.";
    }
}
