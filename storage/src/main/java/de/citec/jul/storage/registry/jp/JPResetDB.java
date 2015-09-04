/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.jp;

import de.citec.jps.core.AbstractJavaProperty.ValueType;
import de.citec.jps.core.JPService;
import de.citec.jps.exception.JPValidationException;
import de.citec.jps.preset.AbstractJPBoolean;
import de.citec.jps.preset.JPTestMode;
import java.io.IOException;

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
            if(!JPService.getProperty(JPTestMode.class).getValue()) {
                logger.warn("=== Type y and press enter to contine ===");
                try {
                    if(!(System.in.read() == 'y')) {
                        throw new JPValidationException("Execution aborted by user!");
                    }
                } catch (IOException ex) {
                    throw new JPValidationException("Validation failed because of invalid input state!", ex);
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Reset the internal database.";
    }
}
