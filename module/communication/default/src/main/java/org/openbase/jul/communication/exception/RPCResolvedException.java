package org.openbase.jul.communication.exception;

/*-
 * #%L
 * JUL Communication Default
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;

/**
 * This class represents an RPCException while the raw message is reconstructed as exception cause chain.
 * This allows a proper exception handling of remote calls.
 */
public class RPCResolvedException extends CouldNotPerformException {

    private final static Logger LOGGER = LoggerFactory.getLogger(RPCResolvedException.class);

    final RPCException rpcException;

    /**
     * Constructor creates a new RPCResolvedException which is than representing the given {@code rpcException} while the message is reconstructed as exception cause chain.
     *
     * @param message      an error description.
     * @param rpcException the RPCException to resolve.
     */
    public RPCResolvedException(final String message, final RPCException rpcException) {
        super(message, resolveRPCException(rpcException));
        this.rpcException = rpcException;
    }

    /**
     * Default constructor only used
     *
     * @param message an error description.
     * @param cause   the cause.
     */
    public RPCResolvedException(final String message, final Throwable cause) {
        super(message, cause);
        this.rpcException = null;
    }

    /**
     * Constructor creates a new RPCResolvedException which is than representing the given {@code rpcException} while the message is reconstructed as exception cause chain.
     *
     * @param rpcException the RPCException to resolve.
     */
    public RPCResolvedException(final RPCException rpcException) {
        this("", rpcException);
    }

    /**
     * Method parses the RPCException message and resolves the causes and messagen and use those to reconstruct the exception chain.
     *
     * @param rpcException the origin RPCException
     *
     * @return the reconstruced excetion cause chain.
     */
    public static Throwable resolveRPCException(final RPCException rpcException) {
        Throwable exception = null;

        // build stacktrace array where each line is stored as entry. entry is extract each line stacktrace into arr
        final String[] stacktrace = ("Caused by: " + rpcException.getMessage()).split("\n");

        // iterate in reverse order to build exception chain.
        for (int i = stacktrace.length - 1; i >= 0; i--) {
            try {
                // only parse cause lines containing the exception class and message.
                if (stacktrace[i].startsWith("Caused by:")) {
                    final String[] causes = stacktrace[i].split(":");
                    final String exceptionClassName = causes[1].substring(1);

                    // System.out.println("parse: " + stacktrace[i]);
                    // System.out.println("match: " + causes.length);

                    final String message = causes.length <= 2 ? "" : stacktrace[i].substring(stacktrace[i].lastIndexOf(exceptionClassName) + exceptionClassName.length() + 2).trim();

                    // detect exception class
                    final Class<Throwable> exceptionClass;
                    try {
                        exceptionClass = (Class<Throwable>) Class.forName(exceptionClassName);

                        // build exception
                        try {
                            // try default constructor
                            exception = exceptionClass.getConstructor(String.class, Throwable.class).newInstance(message, exception);
                        } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                 NoSuchMethodException | ClassCastException | UnsupportedOperationException ex) {
                            try {
                                // try to handle missing fields
                                if (exception == null && message.isEmpty()) {
                                    exception = exceptionClass.getConstructor().newInstance();
                                } else if (exception == null && !message.isEmpty()) {
                                    exception = exceptionClass.getConstructor(String.class).newInstance(message);
                                } else if (exception != null && message.isEmpty()) {
                                    exception = exceptionClass.getConstructor(Throwable.class).newInstance(exception);
                                } else {
                                    throw ex;
                                }
                            } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                                     NoSuchMethodException exx) {
                                throw new CouldNotPerformException("No compatible constructor found!", exx);
                            }
                        }
                        //System.out.println("create "+ exception.getClass().getSimpleName() + ":" + exception.getMessage());
                    } catch (ClassNotFoundException | ClassCastException | CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Exception[" + exceptionClassName + "] could not be recovered because no compatible Constructor(String, Throwable) was available!", ex), LOGGER, LogLevel.WARN);

                        // apply fallback solution
                        exception = new CouldNotPerformException(message, exception);
                    }
                }
            } catch (IndexOutOfBoundsException | NullPointerException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not extract exception cause or message out of Line[" + stacktrace[i] + "]!", ex), LOGGER, LogLevel.WARN);
            }
        }
        return exception;
    }

    /**
     * Method returns the raw RPCException.
     *
     * @return the raw RPCException
     */
    public RPCException getRpcException() {
        return rpcException;
    }
}
