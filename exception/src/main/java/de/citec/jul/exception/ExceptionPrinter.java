/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

import de.citec.jul.exception.MultiException.SourceExceptionEntry;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.slf4j.Logger;

/**
 *
 * @author mpohling
 */
public class ExceptionPrinter {

    /**
     * Print Exception messages without strack trace in non debug mode. Methode prints recusive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param logger logger which is used as message printer.
     * @param th exception stack to print.
     */
    public static <T extends Throwable> void printHistory(final Logger logger, final T th) {
        printHistoryAndReturnThrowable(logger, th);
    }

    private static Throwable printHistoryAndReturnThrowable(final Logger logger, final Throwable th, final String globalPrefix, final String localPrefix) {
        return printHistoryAndReturnThrowable(logger, th, "", "", null);
    }

    /**
     * Print Exception messages without strack trace in non debug mode. Methode prints recusive all messages of the given exception stack to get a history overview of the causes. In verbose mode (app
     * -v) the stacktrace is printed in the end of history.
     *
     * @param <T> Exception type
     * @param logger logger which is used as message printer.
     * @param th exception stack to print.
     * @return the related Throwable returned for further exception handling.
     */
    public static <T extends Throwable> T printHistoryAndReturnThrowable(final Logger logger, final T th) {
        printHistoryAndReturnThrowable(logger, th, "", "");
        logger.error("=====================================");
        return th;
    }

    private static Throwable printHistoryAndReturnThrowable(final Logger logger, final Throwable th, String globalPrefix, final String localPrefix, Throwable parentException) {

        ElementGenerator<SourceExceptionEntry> sourceExceptionEntryGenerator = new ElementGenerator<SourceExceptionEntry>() {

            @Override
            public String generateRoot(SourceExceptionEntry element) {
                return removeNewLines(element.getException().getMessage());
            }

            @Override
            public void printSubElements(String prefix, SourceExceptionEntry element) {
                printHistoryAndReturnThrowable(logger, element.getException().getCause(), prefix, "", null);
            }

            @Override
            public void printRootElement(String prefix, SourceExceptionEntry element) {
                printHistoryAndReturnThrowable(logger, element.getException(), prefix, "", null);
            }
        };

        ElementGenerator<Throwable> throwableGenerator = new ElementGenerator<Throwable>() {

            @Override
            public String generateRoot(Throwable element) {
                return removeNewLines(element.getMessage());
            }

            @Override
            public void printSubElements(String prefix, Throwable element) {
                printHistoryAndReturnThrowable(logger, element.getCause(), prefix, "", null);
            }

            @Override
            public void printRootElement(String prefix, Throwable element) {
                printHistoryAndReturnThrowable(logger, element, prefix, "", null);
            }
        };

        if (th instanceof MultiException) {
            printFlatTree(globalPrefix, ((MultiException) th).getExceptionStack(), sourceExceptionEntryGenerator, logger);
        } else {
            printSequenze(globalPrefix, buildThrowableList(th), throwableGenerator, logger);
        }

//        boolean isRootException = localPrefix.isEmpty();
//        boolean parentIsMultiException = parentException instanceof MultiException;
//
//        Throwable internalThrowable = th.getCause();
//
//        boolean hasCause = internalThrowable != null && !(th instanceof MultiException);
//
//
//
//        if (!hasCause) {
//            logger.error(buildPrefix((parentIsMultiException ? globalPrefix + "╠═" : globalPrefix)) + buildPrefix(localPrefix + "╚══ ") + removeNewLines(th.getMessage()));
//        } else {
//            if (isRootException) {
//                logger.error(buildPrefix((parentIsMultiException ? globalPrefix + "╠═" : globalPrefix)) + buildPrefix(localPrefix + "═╦═ ") + removeNewLines(th.getMessage()));
//            } else {
//                logger.error(buildPrefix((parentIsMultiException ? globalPrefix + "╠═" : globalPrefix)) + buildPrefix(localPrefix + "╚╦═ ") + removeNewLines(th.getMessage()));
//            }
//        }
//
//        if (th instanceof MultiException) {
//            MultiException.ExceptionStack exceptionStack = ((MultiException) th).getExceptionStack();
//
//            Iterator<MultiException.SourceExceptionEntry> iterator = exceptionStack.iterator();
//            MultiException.SourceExceptionEntry entry;
//
//            String tmpGlobalPrefix = globalPrefix;
//            if (isRootException) {
//                tmpGlobalPrefix += "║";
//            }
//
//            while (iterator.hasNext()) {
//                entry = iterator.next();
//
//                // check if this is the last element
////                if (iterator.hasNext()) {
//                printHistoryAndReturnThrowable(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), (parentIsMultiException ? tmpGlobalPrefix + " ╚═" : tmpGlobalPrefix), localPrefix, th);
////                    printHistoryAndReturnThrowable(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), (parentIsMultiException ? tmpGlobalPrefix + " ╠═" : tmpGlobalPrefix), localPrefix, th);
////                } else {
////                    printHistoryAndReturnThrowable(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), tmpGlobalPrefix + " ╚═", localPrefix, th);
////                }
//
////                if (isRootException) {
////                    printHistoryAndReturnThrowable(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), globalPrefix + "╠══", "");
////                } else {
////                    printHistoryAndReturnThrowable(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), globalPrefix + "║  ", "");
////                }
//            }
//        }
//
//        if (hasCause) {
//            printHistoryAndReturnThrowable(logger, internalThrowable, (parentIsMultiException ? globalPrefix + "  " : globalPrefix), localPrefix + " ", th);
//        }
//
////        // Recursive print of all related Exception message without stacktrace.
////        logger.error(buildPrefix(prefix) + removeNewLines(th.getMessage()));
////        Throwable internalThrowable = th.getCause();
////        while (internalThrowable != null) {
////            if (internalThrowable instanceof MultiException) {
////                logger.error(buildPrefix(prefix + ">") + internalThrowable.getClass().getSimpleName() + " [" + removeNewLines(internalThrowable.getMessage()) + "]");
////                MultiException.ExceptionStack exceptionStack = ((MultiException) internalThrowable).getExceptionStack();
////                for (MultiException.SourceExceptionEntry entry : exceptionStack) {
//////                    printHistoryAndReturnThrowable(LoggerFactory.getLogger(entry.getSource().getClass()), new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), prefix + "|===");
////                    printHistoryAndReturnThrowable(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), prefix + "  |===");
////                    // Print normal stacktrace in verbose mode.
////                    if (logger.isDebugEnabled() || JPService.getProperty(JPVerbose.class).getValue()) {
////                        logger.error(removeNewLines(th.getMessage()), th);
////                        return th;
////                    }
////                }
////            } else {
////                logger.error(buildPrefix("     " + prefix) + internalThrowable.getClass().getSimpleName() + " [" + removeNewLines(internalThrowable.getMessage()) + "]");
////            }
////            internalThrowable = internalThrowable.getCause();
////        }
//        // Print normal stacktrace in verbose mode.
////        if (logger.isDebugEnabled() || JPService.getProperty(JPVerbose.class).getValue()) {
////            logger.error(buildPrefix(prefix) + "=====================================");
////            logger.error(buildPrefix(prefix) + removeNewLines(th.getMessage()), th);
////            logger.error(buildPrefix(prefix) + "=====================================");
////            return th;
////        }
        return th;
    }

    private static List<Throwable> buildThrowableList(Throwable ex) {
        List<Throwable> throwableList = new ArrayList<>();
        if(ex == null) {
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

    public static <T> void printSequenze(String prefix, List<T> elementList, ElementGenerator<T> generator, Logger logger) {
        if (elementList.isEmpty()) {
            return;
        } else if (elementList.size() == 1) {
            logger.error(prefix + "═══ " + generator.generateRoot(elementList.get(0)));
            return;
        } else {
            logger.error(prefix + "═╦═ " + generator.generateRoot(elementList.get(0)));
        }

        for (int i = 1; i < elementList.size(); i++) {

            prefix += " ";

            // check if i is last element
            if (i + 1 < elementList.size()) {
                logger.error(prefix + "╚╦═ " + generator.generateRoot(elementList.get(i)));
            } else {
                logger.error(prefix + "╚══ " + generator.generateRoot(elementList.get(i)));
            }
        }
    }

    public static <T> void printFlatTree(String prefix, List<T> elementList, ElementGenerator<T> generator, Logger logger) {
        if (elementList.isEmpty()) {
            return;
        } else if (elementList.size() == 1) {
            generator.printRootElement(prefix + "═══", elementList.get(0));
            generator.printSubElements(prefix, elementList.get(0));
            return;
        } else {
            generator.printRootElement(prefix + "═╦═", elementList.get(0));
            generator.printSubElements(prefix, elementList.get(0));
        }

        prefix += " ";

        for (int i = 1; i < elementList.size(); i++) {
            // check if i is last element
            if (i + 1 < elementList.size()) {
                generator.printRootElement(prefix + "╠═", elementList.get(i));
                generator.printSubElements(prefix + "║ ", elementList.get(i));
            } else {
                generator.printRootElement(prefix + "╚═", elementList.get(i));
                generator.printSubElements(prefix + "  ", elementList.get(i));
            }
        }
    }

    public interface ElementGenerator<E> {

        public String generateRoot(E element);
        public void printRootElement(String prefix, E element);
        public void printSubElements(String prefix, E element);


    }

    private static String buildPrefix(final String prefix) {
        if (prefix.isEmpty()) {
            return "";
        }

//
//        for (int i = prefix.length()-1; i <= 0; i--) {
//            switch(prefix.charAt(i)) {
//            case '
//            }
//
//        }
//        if(prefix.)
//        return prefix + " ";
        return prefix;
    }

    private static String removeNewLines(final String message) {
        if (message == null) {
            return "null";
        }
        return message.replaceAll("\n", "").trim();
    }

    public static String getHistory(final Throwable th) {
        // TODO:mpohling implement!
        return "Test";
    }
}
