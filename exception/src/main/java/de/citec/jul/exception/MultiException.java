/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.exception;

import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 */
public class MultiException extends CouldNotPerformException {

    private final ExceptionStack exceptionStack;

    public MultiException(final String message, final ExceptionStack exceptionStack) {
        super(message, exceptionStack.get(0).getException());
        this.exceptionStack = exceptionStack;
    }

    public ExceptionStack getExceptionStack() {
        return exceptionStack;
    }

    public void printExceptionStack() {
        for (SourceExceptionEntry entry : exceptionStack) {
            LoggerFactory.getLogger(entry.getSource().getClass()).error("Exception from " + entry.getSource().toString() + ":", entry.getException());
        }
    }

    public static ExceptionStack push(final Object source, final Exception exception, ExceptionStack exceptionStack) {
        if (exceptionStack == null) {
            exceptionStack = new MultiException.ExceptionStack();
        }
        exceptionStack.push(source, exception);
        return exceptionStack;
    }

    public static void checkAndThrow(final String message, final ExceptionStack exceptionStack) throws MultiException {
        if (exceptionStack == null || exceptionStack.isEmpty()) {
            return;
        }
        throw new MultiException(message, exceptionStack);
    }
	
	public static boolean containsException(final ExceptionStack exceptionStack) {
		return !(exceptionStack == null || exceptionStack.isEmpty());
	}
    
    public static int size(final ExceptionStack exceptionStack) {
        return exceptionStack == null ? 0 : exceptionStack.size();
    }

    public static class ExceptionStack extends ArrayList<SourceExceptionEntry> {

        public void push(final Object source, final Exception exception) {
            super.add(new SourceExceptionEntry(source, exception));
        }
    }

    public static class SourceExceptionEntry {

        private final Object source;
        private final Throwable exception;

        public SourceExceptionEntry(Object source, Throwable exception) {
            this.source = source;
            this.exception = exception;
        }

        public Object getSource() {
            return source;
        }

        public Throwable getException() {
            return exception;
        }
    }
}
