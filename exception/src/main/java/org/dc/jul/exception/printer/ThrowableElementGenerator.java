package org.dc.jul.exception.printer;

import org.dc.jul.exception.MultiException;

/**
 * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class ThrowableElementGenerator implements ElementGenerator<Throwable> {

    @Override
    public String generateRoot(Throwable element) {
        return ExceptionPrinter.getContext(element);
    }

    @Override
    public void printRootElement(Throwable element, final Printer printer, String rootPrefix, final String childPrefix) {
        printElement(element, printer, rootPrefix, childPrefix);
    }

    @Override
    public void printElement(Throwable element, final Printer printer, String rootPrefix, final String childPrefix) {
        if (element instanceof MultiException) {
            ExceptionPrinter.printHistory(element, printer, rootPrefix, childPrefix + " ║ ");
        } else {
            printer.print(rootPrefix + " " + generateRoot(element));
        }
    }
};
