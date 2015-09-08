package de.citec.jul.exception.printer;

import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPVerbose;
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
        return JPService.getProperty(JPVerbose.class).getValue();
    }

    public String getMessages() {
        return messages;
    }
};
