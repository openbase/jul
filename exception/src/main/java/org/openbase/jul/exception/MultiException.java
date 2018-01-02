package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import java.util.ArrayList;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
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

    public synchronized static ExceptionStack push(final Object source, final Exception exception, ExceptionStack exceptionStack) {
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

        public SourceExceptionEntry(final Object source, final Throwable exception) {
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
