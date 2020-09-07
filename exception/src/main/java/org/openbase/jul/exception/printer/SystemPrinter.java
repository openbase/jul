package org.openbase.jul.exception.printer;

/*-
 * #%L
 * JUL Exception
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

import org.openbase.jps.core.JPService;

import java.io.PrintStream;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class SystemPrinter implements Printer {

    private final PrintStream printStream;
    private final LogLevel logLevel;

    public SystemPrinter(final PrintStream printStream) {
        this(printStream, getDefaultLogLevel(printStream));
    }

    public SystemPrinter(final PrintStream printStream, final LogLevel logLevel) {
        this.logLevel = logLevel;
        this.printStream = printStream;
    }

    @Override
    public void print(String message) {
       printStream.println(message);
    }

    @Override
    public void print(String message, Throwable throwable) {
        printStream.println(message);
        throwable.printStackTrace(printStream);
    }

    @Override
    public boolean isDebugEnabled() {
        return (JPService.debugMode() ||
                JPService.testMode()  ||
                JPService.verboseMode());
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }

    private static LogLevel getDefaultLogLevel(final PrintStream printStream) {

        // if std out than use info, otherwise the error channel as default.
        if(printStream.equals(System.out)) {
            return LogLevel.INFO;
        } else {
            return LogLevel.ERROR;
        }
    }
}
