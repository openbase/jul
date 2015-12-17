package de.citec.jul.exception.printer;

import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPVerbose;
import de.citec.jul.exception.CouldNotPerformException;
import static de.citec.jul.exception.printer.LogLevel.DEBUG;
import static de.citec.jul.exception.printer.LogLevel.ERROR;
import static de.citec.jul.exception.printer.LogLevel.INFO;
import static de.citec.jul.exception.printer.LogLevel.TRACE;
import static de.citec.jul.exception.printer.LogLevel.WARN;
import org.slf4j.Logger;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class LogPrinter implements Printer {

    private final Logger logger;
    private final LogLevel level;

    public LogPrinter(final Logger logger, final LogLevel level) {
        this.logger = logger;
        this.level = level;
    }

    @Override
    public void print(final String message) {
        switch (level) {
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
        switch (level) {
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
            return logger.isDebugEnabled() || JPService.getProperty(JPVerbose.class).getValue();
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            return true;
        }
    }
};
