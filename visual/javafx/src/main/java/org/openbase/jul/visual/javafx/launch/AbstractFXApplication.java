package org.openbase.jul.visual.javafx.launch;

/*-
 * #%L
 * JUL Visual JavaFX
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

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;

public abstract class AbstractFXApplication extends Application {

    /**
     * Method exists the application and prints the given exception as error description.
     *
     * @param ex the reason for the application exit.
     */
    public static void exit(final int exitCode, final Exception ex) {
        System.err.println(JPService.getApplicationName() + " crashed...");
        ExceptionPrinter.printHistory(ex, System.err);
        System.exit(exitCode);
    }

    /**
     * Method exists the application normally.
     */
    public static void exit() {
        System.exit(0);
    }

    /**
     * {@inheritDoc}
     *
     * @param stage {@inheritDoc}
     */
    @Override
    public void start(Stage stage) {
        try {
            System.out.println("start");
            // setup java property service
            JPService.setApplicationName(getClass());
            registerProperties();
            JPService.parseAndExitOnError(getParameters().getRaw());
            stage.setTitle(JPService.getApplicationName());
            stage.setScene(loadScene());
            stage.show();
        } catch (Exception ex) {
            exit(1, new CouldNotPerformException("Could not start " + JPService.getApplicationName(), ex));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws Exception {@inheritDoc}
     */
    @Override
    public void stop() throws Exception {
        try {
            // close gui
            super.stop();
        } catch (Exception ex) {
            exit(255, new CouldNotPerformException("Could not proper stop " + JPService.getApplicationName(), ex));
        }

        // make sure all shutdown deamons are triggered
        exit();
    }

    /**
     * Method can be overloaded if the registration of jpservice properties is needed.
     */
    protected void registerProperties() {
        // Dummy Method, overwrite for registration
    }

    /**
     * Method should return the default scene to configure during startup.
     *
     * @return the default scene.
     *
     * @throws CouldNotPerformException is thrown in case the scene could not be loaded.
     */
    protected abstract Scene loadScene() throws CouldNotPerformException;
}
