package org.openbase.jul.exception;

/*-
 * #%L
 * JUL Exception
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
     *
     * @param th the throwable to detect the message.
     * @return the message as string.
     */
    public static String getInitialCauseMessage(final Throwable th) {
        return getInitialCause(th).getMessage();
    }

    /**
     * Method returns the initial cause of the given throwable.
     *
     * @param th the throwable to detect the message.
     * @return the cause as throwable.
     */
    public static Throwable getInitialCause(final Throwable th) {
        Throwable cause = th;

        while (cause.getCause() != null) {
            cause = cause.getCause();
        }

        return cause;
    }
}
