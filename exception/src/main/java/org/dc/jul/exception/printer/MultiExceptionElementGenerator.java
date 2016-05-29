package org.dc.jul.exception.printer;

import org.dc.jul.exception.MultiException;

/**
 * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class MultiExceptionElementGenerator implements ElementGenerator<MultiException.SourceExceptionEntry> {

    @Override
    public String generateRoot(final MultiException.SourceExceptionEntry element) {
        return ExceptionPrinter.getContext(element.getException());
    }

    @Override
    public void printRootElement(final MultiException.SourceExceptionEntry element, final Printer printer, final String rootPrefix, final String childPrefix) {
        printer.print(rootPrefix + " " + generateRoot(element));
    }

    @Override
    public void printElement(final MultiException.SourceExceptionEntry element, final Printer printer, final String rootPrefix, final String childPrefix) {
        ExceptionPrinter.printHistory(element.getException(), printer, rootPrefix, childPrefix);
    }
};
