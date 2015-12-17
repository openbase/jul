/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.jp;

import org.dc.jps.core.AbstractJavaProperty.ValueType;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPBadArgumentException;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.exception.JPValidationException;
import org.dc.jps.preset.AbstractJPBoolean;
import org.dc.jps.preset.JPTestMode;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
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
