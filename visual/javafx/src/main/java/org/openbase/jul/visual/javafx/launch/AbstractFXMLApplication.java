package org.openbase.jul.visual.javafx.launch;

/*-
 * #%L
 * JUL Visual JavaFX
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import javafx.scene.Scene;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.visual.javafx.control.AbstractFXController;
import org.openbase.jul.visual.javafx.fxml.FXMLProcessor;

public abstract class AbstractFXMLApplication extends AbstractFXApplication {

    private Class<? extends AbstractFXController> controllerClass;

    /**
     * Creates a new application where the given controller in initialized as root node.
     * The fxml file of the controller is auto resolved and loaded.
     * A custom loader can be implemented by overwriting the method {@code loadDefaultFXML()}.
     *
     * @param controllerClass the controller class to specify the root controller node.
     */
    public AbstractFXMLApplication(final Class<? extends AbstractFXController> controllerClass) {
        this.controllerClass = controllerClass;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected Scene loadScene() throws CouldNotPerformException {
        try {
            // setup scene
            Scene scene;
            try {
                scene = new Scene(FXMLProcessor.loadFxmlPane(controllerClass, getClass()));
            } catch (final Exception ex) {
                throw new CouldNotPerformException("Could not load fxml description!", ex);
            }
            try {
                scene.getStylesheets().add(getDefaultCSS());
            } catch (final Exception ex) {
                throw new CouldNotPerformException("Could not load css description!", ex);
            }
            return scene;
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not load scene!", ex);
        }
    }

    /**
     * Method should return an uri to the default css file to be loaded during application start.
     *
     * @return
     */
    protected String getDefaultCSS() {
        return "/styles/main-style.css";
    }
}
