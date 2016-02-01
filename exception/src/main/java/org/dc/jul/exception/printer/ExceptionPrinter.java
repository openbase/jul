/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.exception.printer;

/*
 * #%L
 * JUL Exception
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.MultiException.SourceExceptionEntry;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;

/**
 *
 * @author mpohling
 */
public class ExceptionPrinter {

    private static final String SEPARATOR = "=====================================";

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param th exception stack to print.
     * @param logger the logger used as message printer.
     * @return the related Throwable returned for further exception handling.
     */
    public static <T extends Throwable> T printHistoryAndReturnThrowable(final T th, final Logger logger) {
        return printHistoryAndReturnThrowable(th, logger, LogLevel.ERROR);
    }
    
    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. The logging level is fixed to level "error".
     *
     * @param <T> Exception type
     * @param th exception stack to print.
     * @param logger the logger used as message printer.
     * @param level
     * @return the related Throwable returned for further exception handling.
     */
    public static <T extends Throwable> T printHistoryAndReturnThrowable(final T th, final Logger logger, final LogLevel level) {
        printHistory(th, new LogPrinter(logger, level));
        return th;
    }

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param logger the logger used as message printer.
     * @param th exception stack to print.
     * @param level
     */
    public static <T extends Throwable> void printHistory(final T th, final Logger logger, final LogLevel level) {
        printHistory(th, new LogPrinter(logger, level));
    }

    
    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. The logging level is fixed to level "error".
     *
     * @param <T> Exception type
     * @param th exception stack to print.
     * @param logger
     */
    public static <T extends Throwable> void printHistory(final T th, final Logger logger) {
        printHistory(th, logger, LogLevel.ERROR);
    }

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param th exception stack to print.
     * @param printer the message printer to use.
     * @return the related Throwable returned for further exception handling.
     */
    public static <T extends Throwable> T printHistoryAndReturnThrowable(final T th, final Printer printer) {
        printHistory(th, printer);
        return th;
    }

    /**
     * Prints a human readable Exception cause chain of the given Exception. Builds a human readable Exception Print Exception messages without StackTrace in non debug mode. Method prints recursive
     * all messages of the given exception stack to get a history overview of the causes. In verbose mode (app -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param printer the message printer to use.
     * @param th exception stack to print.
     */
    public static <T extends Throwable> void printHistory(final T th, final Printer printer) {
        printHistory(th, printer, "", "");

        // Print normal stacktrace in debug mode.
        if (printer.isDebugEnabled()) {
            printer.print(SEPARATOR);
            printer.print(removeNewLines(th), th);
        }
        printer.print(SEPARATOR);
    }

    /**
     * Generates a human readable Exception cause chain of the given Exception as String representation.
     *
     * @param th the Throwable to process the StackTrace.
     * @return A History description as String.
     */
    public static String getHistory(final Throwable th) {
        VariablePrinter printer = new VariablePrinter();
        printHistory(th, printer);
        return printer.getMessages();
    }

    private static void printHistory(final Throwable th, final Printer printer, String rootPrefix, final String childPrefix) {

        ElementGenerator<SourceExceptionEntry> sourceExceptionEntryGenerator = new ElementGenerator<SourceExceptionEntry>() {

            @Override
            public String generateRoot(final SourceExceptionEntry element) {
                return removeNewLines(element.getException());
            }

            @Override
            public void printRootElement(final SourceExceptionEntry element, final Printer printer, final String rootPrefix, final String childPrefix) {
                printer.print(rootPrefix + " " + generateRoot(element));
            }

            @Override
            public void printElement(final SourceExceptionEntry element, final Printer printer, final String rootPrefix, final String childPrefix) {
                printHistory(element.getException(), printer, rootPrefix, childPrefix);
            }
        };

        ElementGenerator<Throwable> throwableGenerator = new ElementGenerator<Throwable>() {

            @Override
            public String generateRoot(Throwable element) {
                return removeNewLines(element);
            }

            @Override
            public void printRootElement(Throwable element, final Printer printer, String rootPrefix, final String childPrefix) {
                printElement(element, printer, rootPrefix, childPrefix);
            }

            @Override
            public void printElement(Throwable element, final Printer printer, String rootPrefix, final String childPrefix) {
                printer.print(rootPrefix + " " + generateRoot(element));
            }
        };

        if (th instanceof MultiException) {
            printFlatTree(new SourceExceptionEntry(ExceptionPrinter.class, th), ((MultiException) th).getExceptionStack(), sourceExceptionEntryGenerator, printer, rootPrefix, childPrefix);
        } else {
            printSequenze(buildThrowableList(th), throwableGenerator, printer, rootPrefix, childPrefix);
        }
    }

    private static List<Throwable> buildThrowableList(Throwable ex) {
        List<Throwable> throwableList = new ArrayList<>();
        if (ex == null) {
            return throwableList;
        }
        throwableList.add(ex);

        Throwable cause = ex.getCause();
        while (cause != null) {
            throwableList.add(cause);
            cause = cause.getCause();
        }
        return throwableList;
    }

    private static <T> void printSequenze(final List<T> elementList, final ElementGenerator<T> generator, final Printer printer, final String rootPrefix, final String childPrefix) {
        if (elementList.isEmpty()) {
            return;
        } else if (elementList.size() == 1) {
            generator.printElement(elementList.get(0), printer, rootPrefix + "═══", childPrefix);
            return;
        } else {
            generator.printElement(elementList.get(0), printer, rootPrefix + "═╦═", childPrefix);
        }

        String localPrefix = "";

        for (int i = 1; i < elementList.size(); i++) {

            localPrefix += " ";

            // check if i is last element
            if (i + 1 == elementList.size()) {
                generator.printElement(elementList.get(i), printer, childPrefix + localPrefix + "╚══", childPrefix + " ");
            } else {
                generator.printElement(elementList.get(i), printer, childPrefix + localPrefix + "╚╦═", childPrefix + " ");
            }
        }
    }

    private static <T> void printFlatTree(final T rootElement, final List<T> elementList, final ElementGenerator<T> generator, final Printer printer, final String rootPrefix, final String childPrefix) {
        // Handle Root Element
        if (rootElement == null) {
            return;
        } else if (elementList.isEmpty()) {
            generator.printRootElement(rootElement, printer, rootPrefix + "══ ", childPrefix);
            return;
        } else {
            generator.printRootElement(rootElement, printer, rootPrefix + "═╦═", childPrefix);
        }

        // Handle Child Element
        for (int i = 0; i < elementList.size(); i++) {
            // check if i is last element
            if (i + 1 == elementList.size()) {
                generator.printElement(elementList.get(i), printer, childPrefix + " ╚═", childPrefix + "   ");
            } else {
                generator.printElement(elementList.get(i), printer, childPrefix + " ╠═", childPrefix + " ║ ");
            }
        }
    }

    private interface ElementGenerator<E> {

        public String generateRoot(final E element);

        public void printRootElement(final E element, final Printer printer, final String rootPrefix, final String childPrefix);

        public void printElement(final E element, final Printer printer, final String rootPrefix, final String childPrefix);

    }

    private static String removeNewLines(final Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        if (throwable.getMessage() == null || throwable.getMessage().isEmpty()) {
            return throwable.getClass().getSimpleName();
        }

        return throwable.getMessage().replaceAll("\n", "").trim();
    }
}
