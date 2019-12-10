package org.openbase.jul.visual.javafx.iface;

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

import javafx.application.Platform;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.ExceptionProcessor;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 * Interface can be used for non static panes to provide a unique interface for updating the content.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface DynamicPane extends StaticPane {

    /**
     * Updates dynamic pane content.
     *
     * Note: Can be called from any thread.
     */
    default void update() {
        if (Platform.isFxApplicationThread()) {
            try {
                updateDynamicContent();
            } catch (CouldNotPerformException ex) {
                if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not update all dynamic components!", ex), LoggerFactory.getLogger(getClass()));
                }
            }
            return;
        }
        Platform.runLater(() -> {
            try {
                updateDynamicContent();
            } catch (CouldNotPerformException ex) {
                if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not update all dynamic components!", ex), LoggerFactory.getLogger(getClass()));
                }
            }
        });
    }


    /**
     * Update dynamic pane content.
     *
     * Note: Should only be called via the gui thread!
     */
    void updateDynamicContent() throws CouldNotPerformException;
}
