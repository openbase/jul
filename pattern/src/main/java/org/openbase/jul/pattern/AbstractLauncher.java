package org.openbase.jul.pattern;

/*-
 * #%L
 * JUL Pattern
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Shutdownable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractLauncher implements Launcher {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    public static void main(final String args[], final Class application, final Class<? extends AbstractLauncher>... launchers) {

        final Logger logger = LoggerFactory.getLogger(Launcher.class);
        JPService.setApplicationName(application);

        MultiException.ExceptionStack exceptionStack = null;

        Map<Class<? extends AbstractLauncher>, AbstractLauncher> launcherMap = new HashMap<>();
        for (final Class<? extends AbstractLauncher> launcherClass : launchers) {
            try {
                launcherMap.put(launcherClass, launcherClass.newInstance());
            } catch (InstantiationException | IllegalAccessException ex) {
                exceptionStack = MultiException.push(application, new CouldNotPerformException("Could not load launcher class!", ex), exceptionStack);
            }
        }

        for (final AbstractLauncher launcher : launcherMap.values()) {
            launcher.loadProperties();
        }

        JPService.parseAndExitOnError(args);

        logger.info("Start " + JPService.getApplicationName() + "...");

        try {
            for (final Entry<Class<? extends AbstractLauncher>, AbstractLauncher> launcherEntry : launcherMap.entrySet()) {
                try {
                    launcherEntry.getValue().launch();
                    Shutdownable.registerShutdownHook(launcherEntry.getValue());
                } catch (CouldNotPerformException ex) {
                    exceptionStack = MultiException.push(application, new CouldNotPerformException("Could not launch " + launcherEntry.getKey().getSimpleName() + "!", ex), exceptionStack);
                }
            }

        } catch (InterruptedException ex) {
            ExceptionPrinter.printHistoryAndExit(JPService.getApplicationName() + " catched shutdown signal during startup phase!", ex, logger);
        }

        try {
            MultiException.checkAndThrow("Errors during startup phase!", exceptionStack);
            logger.info(JPService.getApplicationName() + " successfully started.");
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(JPService.getApplicationName() + " was startet with some errors!", ex, logger);
        }
    }
}
