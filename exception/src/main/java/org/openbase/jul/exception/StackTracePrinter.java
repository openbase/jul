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
import java.util.Arrays;
import org.slf4j.Logger;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class StackTracePrinter {

    /**
     * Method prints the stack trace of the calling thread in a human readable way.
     *
     * @param logger the logger used for printing.
     */
    public static void printStackTrace(final Logger logger) {
        StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
        printStackTrace(Arrays.copyOfRange(stackTrace, 2, stackTrace.length), logger);
    }

    /**
     * Method prints the given stack trace in a human readable way.
     *
     * @param stackTraces the stack trace to print.
     * @param logger the logger used for printing.
     */
    public static void printStackTrace(final StackTraceElement[] stackTraces, final Logger logger) {
        String stackTraceString = "";
        for (final StackTraceElement stackTrace : stackTraces) {
            stackTraceString += stackTrace.toString() + "\n";
        }
        logger.info("\n=== Stacktrace ===\n" + stackTraceString + "==================");
    }
}
