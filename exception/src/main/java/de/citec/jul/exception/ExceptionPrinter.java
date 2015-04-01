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
	 * history overview of the causes. In debug mode the stacktrace is printed
	 * in the end of history.
	 *
	 * @param logger logger which is used as message printer.
	 * @param th exception stack to print.
	 * @return the related Throwable returned for further exception handling.
	 */
	public static Throwable printHistory(final Logger logger, final Throwable th) {

		// Recursive print of all related Exception message without stacktrace.
		logger.error(th.getMessage());
		Throwable internalThrowable = th.getCause();
		while (internalThrowable != null) {
			if (internalThrowable instanceof MultiException) {
				MultiException.ExceptionStack exceptionStack = ((MultiException)internalThrowable).getExceptionStack();
				for(MultiException.SourceExceptionEntry entry : exceptionStack) {
					printHistory(LoggerFactory.getLogger(entry.getSource().getClass()), new Exception("Exception from " + entry.getSource().toString() + ":", entry.getException()));
				}
			} else {
				logger.error("=== " + internalThrowable.getClass().getSimpleName() + "[" + internalThrowable.getMessage() + "]");
			}
			internalThrowable = internalThrowable.getCause();
		}

		// Print normal stacktrace in debug mode.
		if (logger.isDebugEnabled() || JPService.getProperty(JPVerbose.class).getValue()) {
			logger.error(th.getMessage(), th);
			return th;
		}

		logger.error("-------------------------------------");
		return th;
	}
}
