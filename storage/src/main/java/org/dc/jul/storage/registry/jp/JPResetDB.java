/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry.jp;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jps.core.AbstractJavaProperty.ValueType;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPBadArgumentException;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.exception.JPValidationException;
import org.dc.jps.preset.AbstractJPBoolean;
import org.dc.jps.preset.JPTestMode;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author mpohling
 */
public class JPResetDB extends AbstractJPBoolean {

    public final static String[] COMMAND_IDENTIFIERS = {"--reset"};

    public JPResetDB() {
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
            logger.warn("WARNING: OVERWRITING CURRENT DATABASE!!!");
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
        return "Reset the internal database.";
    }
}
