/**
 * ==================================================================
 *
 * This file is part of org.openbase.bco.bcozy.
 *
 * org.openbase.bco.bcozy is free software: you can redistribute it and modify
 * it under the terms of the GNU General Public License (Version 3)
 * as published by the Free Software Foundation.
 *
 * org.openbase.bco.bcozy is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with org.openbase.bco.bcozy. If not, see <http://www.gnu.org/licenses/>.
 * ==================================================================
 */
package org.openbase.jul.visual.javafx.iface;

/*-
 * #%L
 * JUL Visual JavaFX
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

import javafx.application.Platform;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.iface.DefaultInitializable;
import org.openbase.jul.schedule.SyncObject;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface StaticPane extends DefaultInitializable {

    /**
     * Init pane content.
     */
    void initContent();

    @Override
    default void init() throws InitializationException, InterruptedException {
        if (Platform.isFxApplicationThread()) {
            initContent();
        }

        final SyncObject initSync = new SyncObject("StaticPaneInitSync");
        synchronized (initSync) {
            Platform.runLater(() -> {
                initContent();
                synchronized (initSync) {
                    initSync.notifyAll();
                }
            });
            initSync.wait();
        }
    }
}
