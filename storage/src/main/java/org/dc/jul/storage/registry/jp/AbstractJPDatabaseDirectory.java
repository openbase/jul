/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry.jp;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.exception.JPValidationException;
import org.dc.jps.preset.AbstractJPDirectory;
import org.dc.jps.preset.JPHelp;
import org.dc.jps.tools.FileHandler;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public abstract class AbstractJPDatabaseDirectory extends AbstractJPDirectory {

    public static FileHandler.ExistenceHandling existenceHandling = FileHandler.ExistenceHandling.Must;
    public static FileHandler.AutoMode autoMode = FileHandler.AutoMode.Off;

    public AbstractJPDatabaseDirectory(String[] commandIdentifier) {
        super(commandIdentifier, existenceHandling, autoMode);
    }

    @Override
    public void validate() throws JPValidationException {

        boolean reinitDetected = false;

        try {
            if (JPService.getProperty(JPInitializeDB.class).getValue()) {
                setAutoCreateMode(FileHandler.AutoMode.On);
                setExistenceHandling(FileHandler.ExistenceHandling.Must);
                reinitDetected = true;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        try {
            if (JPService.getProperty(JPResetDB.class).getValue()) {
                setAutoCreateMode(FileHandler.AutoMode.On);
                setExistenceHandling(FileHandler.ExistenceHandling.MustBeNew);
                reinitDetected = true;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        if (!getValue().exists() && !reinitDetected) {
            throw new JPValidationException("Could not detect Database["+getValue().getAbsolutePath()+"]! You can use the argument " + JPInitializeDB.COMMAND_IDENTIFIERS[0] + " to initialize a new db enviroment. Use " + JPHelp.COMMAND_IDENTIFIERS[0] + " to get more options.");
        }

        super.validate();
    }
}
