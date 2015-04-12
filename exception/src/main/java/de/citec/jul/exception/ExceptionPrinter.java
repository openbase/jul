/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPVerbose;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 */
public class ExceptionPrinter {

    /**
     * Print Exception messages without strack trace in non debug mode. Methode
     * prints recusive all messages of the given exception stack to get a
     * history overview of the causes. In verbose mode (app -v) the stacktrace
     * is printed in the end of history.
     *
     * @param <T> Exception type
     * @param logger logger which is used as message printer.
     * @param th exception stack to print.
     * @return the related Throwable returned for further exception handling.
     */
    public static <T extends Throwable>  T printHistory(final Logger logger, final T th) {
        Throwable throwable = printHistory(logger, th, "");
        logger.error("=====================================");
        return th;
    }

    private static Throwable printHistory(final Logger logger, final Throwable th, final String prefix) {

        // Recursive print of all related Exception message without stacktrace.
        logger.error(buildPrefix(prefix) + removeNewLines(th.getMessage()));
        Throwable internalThrowable = th.getCause();
        while (internalThrowable != null) {
            if (internalThrowable instanceof MultiException) {
                logger.error(buildPrefix(prefix + ">") + internalThrowable.getClass().getSimpleName() + " [" + removeNewLines(internalThrowable.getMessage()) + "]");
                MultiException.ExceptionStack exceptionStack = ((MultiException) internalThrowable).getExceptionStack();
                for (MultiException.SourceExceptionEntry entry : exceptionStack) {
//                    printHistory(LoggerFactory.getLogger(entry.getSource().getClass()), new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), prefix + "|===");
                    printHistory(logger, new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()), prefix + "  |===");
                    // Print normal stacktrace in verbose mode.
                    if (logger.isDebugEnabled() || JPService.getProperty(JPVerbose.class).getValue()) {
                        logger.error(removeNewLines(th.getMessage()), th);
                        return th;
                    }
                }
            } else {
                logger.error(buildPrefix("     " + prefix) + internalThrowable.getClass().getSimpleName() + " [" + removeNewLines(internalThrowable.getMessage()) + "]");
            }
            internalThrowable = internalThrowable.getCause();
        }
        
        // Print normal stacktrace in verbose mode.
        if (logger.isDebugEnabled() || JPService.getProperty(JPVerbose.class).getValue()) {
            logger.error(buildPrefix(prefix) + "=====================================");
            logger.error(buildPrefix(prefix)+removeNewLines(th.getMessage()), th);
            logger.error(buildPrefix(prefix) + "=====================================");
            return th;
        }
        return th;
    }

    private static String buildPrefix(final String prefix) {
        if (prefix.isEmpty()) {
            return "";
        }
        return prefix + " ";
    }

    private static String removeNewLines(final String message) {
        if (message == null) {
            return "null";
        }
        return message.replaceAll("\n", "").trim();
    }
}
