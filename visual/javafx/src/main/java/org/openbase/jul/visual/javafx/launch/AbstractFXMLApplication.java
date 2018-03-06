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

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.visual.javafx.fxml.FXMLProcessor;

public abstract class AbstractFXMLApplication extends AbstractFXApplication {

    /**
     * {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    protected Scene loadScene() throws CouldNotPerformException {
        try {
            // setup scene
            Scene scene;
            try {
                scene = new Scene(FXMLProcessor.loadFxmlPane(getDefaultFXML(), getClass()));
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
     * @return
     */
    protected abstract String getDefaultCSS();

    /**
     * Method should return an uri to the default fxml file to be loaded during application start.
     * @return
     */
    protected abstract String getDefaultFXML();

}
