package org.openbase.jul.exception;

/*-
 * #%L
 * JUL Exception
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
 * @author vdasilva
 */
public class ExceptionProcessor {

    /**
     * Constructor to avoid instantiation.
     */
    private ExceptionProcessor() {
    }

    /**
     * Method returns the message of the initial cause of the given throwable.
     * If the throwable does not provide a message its class name is returned.
     *
     * @param throwable the throwable to detect the message.
     *
     * @return the message as string.
     */
    public static String getInitialCauseMessage(final Throwable throwable) {
        final Throwable cause = getInitialCause(throwable);
        if (cause.getLocalizedMessage() == null) {
            return cause.getClass().getSimpleName();
        }
        return cause.getLocalizedMessage();
    }

    /**
     * Method returns the initial cause of the given throwable.
     *
     * @param throwable the throwable to detect the message.
     *
     * @return the cause as throwable.
     */
    public static Throwable getInitialCause(final Throwable throwable) {
        if (throwable == null) {
            new FatalImplementationErrorException(ExceptionProcessor.class, new NotAvailableException("cause"));
        }

        Throwable cause = throwable;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        return cause;
    }

    /**
     * Set the given {@code initialCause} as initial cause of the given {@code throwable}.
     *
     * @param throwable    the throwable to extend.
     * @param initialCause the new initial cause.
     *
     * @return the new cause chain.
     */
    public static Throwable setInitialCause(final Throwable throwable, final Throwable initialCause) {
        getInitialCause(throwable).initCause(initialCause);
        return throwable;
    }

    /**
     * Method checks if the initial cause of the given throwable is related to any system shutdown routine.
     * In more detail, an initial cause is related to the system shutdown when it is an instance of the {@code ShutdownInProgressException} class.
     *
     * @param throwable the top level cause.
     *
     * @return returns true if the given throwable is caused by a system shutdown, otherwise false.
     */
    public static boolean isCausedBySystemShutdown(final Throwable throwable) {
        return ExceptionProcessor.getInitialCause(throwable) instanceof ShutdownInProgressException;
    }

    /**
     * Method checks if any cause of the given throwable is related to any thread interruption.
     *
     * @param throwable the top level cause.
     *
     * @return returns true if the given throwable is caused by any thread interruption, otherwise false.
     */
    public static boolean isCausedByInterruption(final Throwable throwable) {
        if (throwable == null) {
            new FatalImplementationErrorException(ExceptionProcessor.class, new NotAvailableException("cause"));
            return false;
        }

        Throwable cause = throwable;

        // initial check
        if (cause instanceof InterruptedException) {
            return true;
        }

        // check causes
        while (cause.getCause() != null) {
            cause = cause.getCause();
            if (cause instanceof InterruptedException) {
                return true;
            }
        }

        // no interruption found
        return false;
    }

    /**
     * Method throws an interrupted exception if the given {@code throwable} is caused by a system shutdown.
     *
     * @param throwable the throwable to check.
     * @param <T>       the type of the {@code throwable}
     *
     * @return the bypassed {@code throwable}
     *
     * @throws InterruptedException is thrown if the system shutdown was initiated.
     */
    public static <T extends Throwable> T interruptOnShutdown(final T throwable) throws InterruptedException {
        if (ExceptionProcessor.isCausedBySystemShutdown(throwable)) {
            throw new InterruptedException(getInitialCauseMessage(throwable));
        } else {
            return throwable;
        }
    }
}
