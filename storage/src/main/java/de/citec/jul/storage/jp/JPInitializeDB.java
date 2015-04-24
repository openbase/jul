/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.jp;

import de.citec.jps.core.AbstractJavaProperty.ValueType;
import de.citec.jps.core.JPService;
import de.citec.jps.preset.AbstractJPBoolean;
import de.citec.jps.preset.JPTestMode;

/**
 *
 * @author mpohling
 */
public class JPInitializeDB extends AbstractJPBoolean {

    public final static String[] COMMAND_IDENTIFIERS = {"--init"};

    public JPInitializeDB() {
        super(COMMAND_IDENTIFIERS);
    }
    
    @Override
    protected Boolean getPropertyDefaultValue() {
        return false;
    }

    @Override
    public void validate() throws Exception {
        super.validate();
        if (getValueType().equals((ValueType.CommandLine))) {
            logger.warn("WARNING: OVERWRITING CURRENT DATABASE!!!");
            if(!JPService.getProperty(JPTestMode.class).getValue()) {
                logger.warn("=== Type y and press enter to contine ===");
                if(!(System.in.read() == 'y')) {
                    System.exit(1);
                }
            }
        }
    }

    @Override
    public String getDescription() {
        return "Initialize a new instance of the interal database.";
    }
}
