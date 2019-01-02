package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class MultiException extends CouldNotPerformException {

    private static final Logger LOGGER = LoggerFactory.getLogger(MultiException.class);

    private final ExceptionStack exceptionStack;

    public MultiException(final String message, final Throwable cause) {
        super(message, cause);
        this.exceptionStack = new ExceptionStack();
    }

    public MultiException(final String message, final ExceptionStack exceptionStack) {
        super(message, exceptionStack.get(0).getException());
        this.exceptionStack = exceptionStack;
    }

    public synchronized static ExceptionStack push(final Object source, final Exception exception, ExceptionStack exceptionStack) {
        if (exceptionStack == null) {
            exceptionStack = new MultiException.ExceptionStack();
        }
        exceptionStack.push(source, exception);
        return exceptionStack;
    }

    /**
     * Method throws an exception if the given {@code exceptionStack} contains any exceptions, otherwise this method has no effect.
     * <p>
     * Note: The message provider is only used in case the {@code MultiException} is generated, which means any operations within the call body are only performed in case of an exception.
     *
     * @param messageProvider the message provider is used to deliver the {@code MultiException} headline message in case the {@code exceptionStack} contains any exceptions.
     * @param exceptionStack  the stack to check.
     *
     * @throws MultiException is thrown if the {@code exceptionStack} contains any exceptions.
     */
    public static void checkAndThrow(final Callable<String> messageProvider, final ExceptionStack exceptionStack) throws MultiException {
        if (exceptionStack == null || exceptionStack.isEmpty()) {
            return;
        }

        String message;
        try {
            message = messageProvider.call();
        } catch (Exception ex) {
            message = "?";
            ExceptionPrinter.printHistory(new NotAvailableException("MultiException", "Message", ex), LOGGER);
        }
        throw new MultiException(message, exceptionStack);
    }

    /**
     * @param message the message used as {@code MultiException} headline message..
     * @param exceptionStack  the stack to check.
     *
     * @throws MultiException
     * @deprecated please use {@code checkAndThrow(final Callable<String> messageProvider, final ExceptionStack exceptionStack)} out of performance reasons.
     */
    @Deprecated
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

    public ExceptionStack getExceptionStack() {
        return exceptionStack;
    }

    public void printExceptionStack() {
        for (SourceExceptionEntry entry : exceptionStack) {
            LoggerFactory.getLogger(entry.getSource().getClass()).error("Exception from " + entry.getSource().toString() + ":", entry.getException());
        }
    }

    public static class ExceptionStack extends ArrayList<SourceExceptionEntry> {

        public ExceptionStack() {
        }

        public ExceptionStack(final Collection<? extends SourceExceptionEntry> collection) {
            super(collection);
        }

        public ExceptionStack(int initialCapacity) {
            super(initialCapacity);
        }

        public void push(final Object source, final Exception exception) {
            super.add(new SourceExceptionEntry(source, exception));
        }
    }

    public static class SourceExceptionEntry {

        private final Object source;
        private final Throwable exception;

        public SourceExceptionEntry(final Object source, final Throwable exception) {
            if (source != null) {
                this.source = source;
            } else {
                this.source = "";
            }
            if (exception != null) {
                this.exception = exception;
            } else {
                this.exception = new FatalImplementationErrorException("Unknown exception!", this);
            }
        }

        public Object getSource() {
            return source;
        }

        public Throwable getException() {
            return exception;
        }
    }
}
