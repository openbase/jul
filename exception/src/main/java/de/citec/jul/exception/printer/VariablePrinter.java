package de.citec.jul.exception.printer;

import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPVerbose;
import de.citec.jul.exception.CouldNotPerformException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class VariablePrinter implements Printer {

    private static final Logger logger = LoggerFactory.getLogger(VariablePrinter.class);

    private String messages = "";

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
            logger.error("Could not print stacktrace!", ex);
        }
    }

    @Override
    public boolean isDebugEnabled() {
        try {
            return JPService.getProperty(JPVerbose.class).getValue();
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            return true;
        }
    }

    public String getMessages() {
        return messages;
    }
};
