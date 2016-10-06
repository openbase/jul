package org.openbase.jul.exception.printer;

/*
 * #%L
 * JUL Exception
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.MultiException.SourceExceptionEntry;
import org.slf4j.Logger;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ExceptionPrinter {

    private static final String SEPARATOR = "=====================================";
    private static final ElementGenerator<MultiException.SourceExceptionEntry> MULTI_EXCEPTION_ELEMENT_GENERATOR = new MultiExceptionElementGenerator();
    private static final ElementGenerator<Throwable> THROWABLE_ELEMENT_GENERATOR = new ThrowableElementGenerator();
    private static Boolean beQuiet = false;

    public static void setBeQuit(Boolean beQuiet) {
        ExceptionPrinter.beQuiet = beQuiet;
    }

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
     * Print Exception messages without stack trace in non debug mode and call system exit afterwards. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. The logging level is fixed to level "error". After printing the system exit routine with error code 255 is triggered.
     *
     * @param <T> Exception type
     * @param th exception stack to print.
     * @param logger
     */
    public static <T extends Throwable> void printHistoryAndExit(final T th, final Logger logger) {
        printHistory(th, logger, LogLevel.ERROR);
        System.exit(255);
    }

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. The logging level is fixed to level "error". The given message and the exception are bundled as new CouldNotPerformException and further processed.
     *
     * @param <T> Exception type
     * @param message the reason why this exception occurs.
     * @param th exception cause.
     * @param logger
     */
    public static <T extends Throwable> void printHistory(final String message, T th, final Logger logger) {
        printHistory(new CouldNotPerformException(message, th), logger, LogLevel.ERROR);
    }

    /**
     * Print Exception messages without stack trace in non debug mode and call system exit afterwards. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. The logging level is fixed to level "error". The given message and the exception are bundled as new CouldNotPerformException and further processed.
     * After printing the system exit routine with error code 255 is triggered.
     *
     * @param <T> Exception type
     * @param message the reason why this exception occurs.
     * @param th exception cause.
     * @param logger
     */
    public static <T extends Throwable> void printHistoryAndExit(final String message, T th, final Logger logger) {
        printHistory(new CouldNotPerformException(message, th), logger, LogLevel.ERROR);
    }

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param th the exception stack to print.
     * @param stream the stream used for printing the message history e.g. System.out or. System.err
     */
    public static <T extends Throwable> void printHistory(final T th, final PrintStream stream) {
        printHistory(th, new SystemPrinter(stream));
    }

    /**
     * Print Exception messages without stack trace in non debug mode and call system exit afterwards. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. After printing the system exit routine with error code 255 is triggered.
     *
     * @param <T> Exception type
     * @param th the exception stack to print.
     * @param stream the stream used for printing the message history e.g. System.out or. System.err
     */
    public static <T extends Throwable> void printHistoryAndExit(final T th, final PrintStream stream) {
        printHistory(th, new SystemPrinter(stream));
        System.exit(255);
    }

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history. The given message and the exception are bundled as new CouldNotPerformException and further processed.
     *
     * @param <T> Exception type
     * @param message the reason why this exception occurs.
     * @param th the exception cause.
     * @param stream the stream used for printing the message history e.g. System.out or. System.err
     */
    public static <T extends Throwable> void printHistory(final String message, final T th, final PrintStream stream) {
        printHistory(new CouldNotPerformException(message, th), new SystemPrinter(stream));
    }

    /**
     * Print Exception messages without stack trace in non debug mode and call system exit afterwards. Method prints recursive all messages of the given exception stack to get a history overview of the causes.
     * In verbose mode (app -v) the stacktrace is printed in the end of history. The given message and the exception are bundled as new CouldNotPerformException and further processed.
     * After printing the system exit routine with error code 255 is triggered.
     *
     * @param <T> Exception type
     * @param message the reason why this exception occurs.
     * @param th the exception cause.
     * @param stream the stream used for printing the message history e.g. System.out or. System.err
     */
    public static <T extends Throwable> void printHistoryAndExit(final String message, final T th, final PrintStream stream) {
        printHistory(new CouldNotPerformException(message, th), new SystemPrinter(stream));
    }

    /**
     * Print Exception messages without stack trace in non debug mode. Method prints recursive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param th exception stack to print.
     * @param stream the stream used for printing the message history e.g. System.out or. System.err
     * * @return the related Throwable returned for further exception handling.
     */
    public static <T extends Throwable> T printHistoryAndReturnThrowable(final T th, final PrintStream stream) {
        printHistory(th, new SystemPrinter(stream));
        return th;
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
        if (beQuiet) {
            return;
        }
        printHistory(th, printer, "", "");

        // Print normal stacktrace in debug mode for all errors.
        if (printer.isDebugEnabled()) {
            printer.print(SEPARATOR);
            printer.print(getContext(th), th);
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

    static void printHistory(final Throwable th, final Printer printer, String rootPrefix, final String childPrefix) {
        if (beQuiet) {
            return;
        }
        if (th instanceof MultiException) {
            printFlatTree(new SourceExceptionEntry(ExceptionPrinter.class, th), ((MultiException) th).getExceptionStack(), MULTI_EXCEPTION_ELEMENT_GENERATOR, printer, rootPrefix, childPrefix);
        } else {
            printSequenze(buildThrowableList(th), THROWABLE_ELEMENT_GENERATOR, printer, rootPrefix, childPrefix);
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

        String offset = "";

        for (int i = 1; i < elementList.size(); i++) {

            // update offset
            offset += " ";

            // check if i is last element
            if (i + 1 == elementList.size()) {
                generator.printElement(elementList.get(i), printer, childPrefix + offset + "╚══", childPrefix + offset);
            } else {
                generator.printElement(elementList.get(i), printer, childPrefix + offset + "╚╦═", childPrefix + offset);
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

    public static String getContext(final Throwable throwable) {
        if (throwable == null) {
            return "";
        }

        if (throwable.getMessage() == null || throwable.getMessage().isEmpty()) {
            return throwable.getClass().getSimpleName();
        }

        return throwable.getMessage().replaceAll("\n", "").trim();
    }
}
