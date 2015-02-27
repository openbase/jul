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
        super(message);
        this.exceptionStack = exceptionStack;
    }

	public ExceptionStack getExceptionStack() {
		return exceptionStack;
	}

	public void printExceptionStack() {
		for(SourceExceptionEntry entry : exceptionStack) {
			LoggerFactory.getLogger(entry.getSource().getClass()).error("Exception from "+entry.getSource().toString()+":", entry.getException());
		}
	}
    
    public static class ExceptionStack extends ArrayList<SourceExceptionEntry> {

        public void add(Object source, Exception exception) {
           super.add(new SourceExceptionEntry(source, exception));
        }
        
        public void checkAndThrow(String message) throws MultiException {
            if(!isEmpty()) {
                throw new MultiException(message, this);
            }
        }
    }
    
    public static class SourceExceptionEntry {
        private final Object source;
        private final Exception exception;

        public SourceExceptionEntry(Object source, Exception exception) {
            this.source = source;
            this.exception = exception;
        }

        public Object getSource() {
            return source;
        }

        public Exception getException() {
            return exception;
        }
    }
}
