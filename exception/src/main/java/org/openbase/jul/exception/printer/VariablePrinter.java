package org.openbase.jul.exception.printer;

/*
 * #%L
 * JUL Exception
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
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class VariablePrinter implements Printer {

    private static final Logger logger = LoggerFactory.getLogger(VariablePrinter.class);

    private String messages = "";
    private final LogLevel logLevel;

    public VariablePrinter() {
        this(LogLevel.INFO);
    }

    public VariablePrinter(final LogLevel logLevel) {
        this.logLevel = logLevel;
    }

    @Override
    public void print(final String message) {
        messages += message + "\n";
    }

    @Override
    public void print(String message, Throwable throwable) {
        messages += message;

        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        throwable.printStackTrace(pw);
        messages += sw.toString();
        try {
            sw.close();
        } catch (IOException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not print stacktrace!", ex), logger);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        return (JPService.debugMode() ||
                JPService.testMode()  ||
                JPService.verboseMode());
    }

    public String getMessages() {
        return messages;
    }

    @Override
    public LogLevel getLogLevel() {
        return logLevel;
    }
}
