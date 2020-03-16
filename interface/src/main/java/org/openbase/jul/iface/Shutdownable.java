package org.openbase.jul.iface;

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.ShutdownInProgressException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/*
 * #%L
 * JUL Interface
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public interface Shutdownable {

    /**
     * This method initializes the shutdown phrase of this instance.
     *
     * All resources will be released. In case of any errors no exception will/should be thrown and the method will/should not block. These behavior guarantees a proper component shutdown without
     * skipping any parts because of exception handling.
     */
    void shutdown();

    /**
     * Method registers a runtime shutdown hook for the given Shutdownable. In case the application is finalizing the shutdown method of the Shutdownable will be invoked.
     *
     * Note: If the shutdown was executed on the {@code shutdownable} before system exit you can cancel the {@code ShutdownDaemon} by the provided {@code ShutdownDaemon.cancel()} method to avoid duplicated instance shutdowns.
     *
     * @param shutdownable the instance which is automatically shutting down in case the application is finalizing.
     * @return the for the automated shutdown responsible {@code ShutdownDaemon}.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the shutdown hook could not be registered properly.
     */
    static ShutdownDaemon registerShutdownHook(final Shutdownable shutdownable) throws CouldNotPerformException {
        return new ShutdownDaemon(shutdownable, 0);
    }

    /**
     * Method registers a runtime shutdown hook for the given Shutdownable. In case the application is finalizing the shutdown method of the Shutdownable will be invoked. The given delay can be used
     * to delay the shutdown.
     *
     * Note: This method should be used with care because to delay the shutdown process can result in skipping the shutdown method call in case the operating system mark this application as not
     * responding.
     *
     * @param shutdownable the instance which is automatically shutting down in case the application is finalizing.
     * @param shutdownDelay this time in milliseconds defines the delay of the shutdown after the application shutdown was initiated.
     * @return the for the automated shutdown responsible {@code ShutdownDaemon}.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the shutdown hook could not be registered properly.
     */
    static ShutdownDaemon registerShutdownHook(final Shutdownable shutdownable, final long shutdownDelay) throws CouldNotPerformException {
        return new ShutdownDaemon(shutdownable, shutdownDelay);
    }

    class ShutdownDaemon extends Thread {

        private final static Logger LOGGER = LoggerFactory.getLogger(ShutdownDaemon.class);

        private Shutdownable shutdownable;
        private final long delay;
        private transient boolean canceled;
        private transient boolean shutdown;

        private ShutdownDaemon(final Shutdownable shutdownable, final long delay) throws CouldNotPerformException {
            super(ShutdownDaemon.class.getSimpleName() + "[" + shutdownable + "]");

            if (shutdownable == null) {
                throw new FatalImplementationErrorException("shutdownable argument is missing!", this, new NotAvailableException("shutdownable"));
            }

            this.shutdownable = shutdownable;
            this.delay = delay;
            this.canceled = false;
            this.shutdown = false;
            try {
                Runtime.getRuntime().addShutdownHook(this);
            } catch (IllegalStateException ex) {
                if (ex.getMessage().equals("Shutdown in progress")) {
                    throw new ShutdownInProgressException(Runtime.class);
                }
            }
        }

        @Override
        public void run() {
            shutdown = true;
            try {
                if (delay > 0) {
                    try {
                        Thread.sleep(delay);
                    } catch (InterruptedException ex) {
                        // skip delay and continue shutdown
                    }
                }
                if (!canceled && shutdownable != null) {
                    shutdownable.shutdown();
                }
            } catch (Exception ex) {
                ExceptionPrinter.printHistory("Could not shutdown " + shutdownable + "!", ex, LOGGER);
            }
            shutdownable = null;
        }

        public void cancel() {
            // check if the cancel is maybe called by this shutdown hook, than ignore the cancel. 
            if (!isAlive()) {
                try {
                    Runtime.getRuntime().removeShutdownHook(this);
                    shutdownable = null;
                } catch (IllegalStateException ex) {
                    // Is thrown if the shutdown is already in progress. This could be if there are two shutdown hooks registered for the same shutdownable.
                    canceled = true;
                }
            }
        }

        public boolean isShutdownInProgress() {
            return shutdown;
        }
    }
}
