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
/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ShutdownException extends CouldNotPerformException {

    /**
     * This exception can be used to print a shutdown exception.
     * This exception should not be used as exception handling of any shutdown methods because the should always return without any exceptions to grantee the overall shutdown sequence.
     * Instead exceptions should be printed via the ExceptionPrinter.
     *
     * In case the cause is an instance of an InterruptedException, the interrupted state of the current thread is recovered so no further handling is needed.
     *
     * @param context the instance which could not be shutdown.
     * @param cause the cause of this exception.
     */
    public ShutdownException(Object context, Throwable cause) {
        this(context.getClass(), cause);
    }

    /**
     * This exception can be used to print a shutdown exception.
     * This exception should not be used as exception handling of any shutdown methods because the should always return without any exceptions to gurantee the overall shutdown sequence.
     * Instead exceptions should be printed via the ExceptionPrinter.
     *
     * In case the cause is an instance of an InterruptedException, the interrupted state of the current thread is recovered so no further handling is needed.
     *
     * @param context the instance which could not be shutdown.
     * @param cause the cause of this exception.
     */
    public ShutdownException(Class context, Throwable cause) {
        super("Could not shutdown " + context + "!", cause);
        if (cause instanceof InterruptedException) {
            Thread.currentThread().interrupt();
        }
    }
}
