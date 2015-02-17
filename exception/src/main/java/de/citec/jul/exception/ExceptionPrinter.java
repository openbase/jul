/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

import org.slf4j.Logger;

/**
 *
 * @author mpohling
 */
public class ExceptionPrinter {

    
    /**
     * Print Exception messages without strack trace in non debug mode.
     * Methode prints recusive all messages of the given exception stack to get a history overview of the causes.
     * In debug mode the stacktrace is printed in the end of history.
     * 
     * @param logger logger with is used as message printer.
     * @param th exception stack to print.
     * @return the related Throwable returned for further exception handling.
     */
    public static Throwable printHistory(final Logger logger, final Throwable th) {

        // Recursive print of all related Exception message without stacktrace.
        logger.error(th.getMessage());
        Throwable internalThrowable = th.getCause();
        while (internalThrowable != null) {
            logger.error("=== "+internalThrowable.getClass().getSimpleName() + "[" + internalThrowable.getMessage()+"]");
            internalThrowable = internalThrowable.getCause();
        }
        
        // Print normal stacktrace in debug mode.
        if (logger.isDebugEnabled()) {
            logger.error(th.getMessage(), th);
            return th;
        }
        
        logger.error("-------------------------------------");
        return th;
    }
}
