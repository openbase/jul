package org.openbase.jul.exception.printer;

/*
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jps.preset.JPVerbose;
import org.openbase.jul.exception.CouldNotPerformException;
import static org.openbase.jul.exception.printer.LogLevel.DEBUG;
import static org.openbase.jul.exception.printer.LogLevel.ERROR;
import static org.openbase.jul.exception.printer.LogLevel.INFO;
import static org.openbase.jul.exception.printer.LogLevel.TRACE;
import static org.openbase.jul.exception.printer.LogLevel.WARN;
import org.slf4j.Logger;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class LogPrinter implements Printer {

    private final Logger logger;
    private final LogLevel logLevel;

    public LogPrinter(final Logger logger, final LogLevel logLevel) {
        this.logger = logger;
        this.logLevel = logLevel;
    }

    @Override
    public void print(final String message) {
        switch (logLevel) {
            case TRACE:
                logger.trace(message);
                break;
            case DEBUG:
                logger.debug(message);
                break;
            case INFO:
                logger.info(message);
                break;
            case WARN:
                logger.warn(message);
                break;
            case ERROR:
                logger.error(message);
                break;
        }
    }

    @Override
    public void print(String message, Throwable throwable) {
        switch (logLevel) {
            case TRACE:
                logger.trace(message, throwable);
                break;
            case DEBUG:
                logger.debug(message, throwable);
                break;
            case INFO:
                logger.info(message, throwable);
                break;
            case WARN:
                logger.warn(message, throwable);
                break;
            case ERROR:
                logger.error(message, throwable);
                break;
        }
    }

    @Override
    public boolean isDebugEnabled() {
        try {
            return logger.isDebugEnabled() || (JPService.getProperty(JPVerbose.class).getValue() && logLevel == LogLevel.ERROR);
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            return true;
        }
    }

    public LogLevel getLogLevel() {
        return logLevel;
    }
};
