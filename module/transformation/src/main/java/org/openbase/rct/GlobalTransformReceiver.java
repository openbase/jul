package org.openbase.rct;

/*-
 * #%L
 * RCT
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.ShutdownInProgressException;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class GlobalTransformReceiver {

    private static boolean shutdownInProgress = false;
    private static TransformReceiver instance;

    static {
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                shutdownInProgress = true;
                if (instance != null) {
                    instance.shutdown();
                    instance = null;
                }
            }
        });
    }

    public static synchronized TransformReceiver getInstance() throws NotAvailableException {
        try {
            if (shutdownInProgress) {
                throw new ShutdownInProgressException(GlobalTransformReceiver.class.getSimpleName());
            }
            if (instance == null) {
                try {
                    instance = TransformerFactory.getInstance().createTransformReceiver();
                } catch (TransformerFactory.TransformerFactoryException ex) {
                    throw new CouldNotPerformException("Could not establish rct receiver connection.", ex);
                }
            }
            return instance;
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("TransformReceiver", ex);
        }
    }

    /**
     * Method shutdowns the global instance.
     * Method is limited to the test mode.
     */
    public static synchronized void shutdown() {
        if (JPService.testMode()) {
            if (instance != null) {
                instance.shutdown();
                instance = null;
            }
        }
    }
}
