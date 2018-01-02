package org.openbase.jul.exception;

/*-
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
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.slf4j.LoggerFactory;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class FatalImplementationErrorException extends CouldNotPerformException {

    /**
     * Constructor creates an new FatalImplementationErrorException and prints the exception stack via the jul exception printer. *
     *
     * Note: In case assertions are enabled this instantiation directly results in an assertion exception.
     *
     * @param message the message which describes the fatal implementation error.
     * @param source the instance where the error has been occurred.
     */
    public FatalImplementationErrorException(final String message, final Object source) {
        super("Fatal implementation error in or by using " + source + ": " + message);
        ExceptionPrinter.printHistory(this, LoggerFactory.getLogger(detectClass(source).getClass()));
        assert false;
    }

    /**
     * Constructor creates an new FatalImplementationErrorException and prints the exception stack via the jul exception printer. *
     *
     * Note: In case assertions are enabled this instantiation directly results in an assertion exception.
     *
     * @param message the message which describes the fatal implementation error.
     * @param source the instance where the error has been occurred.
     * @param cause the exception which causes the fatal implementation error.
     */
    public FatalImplementationErrorException(final String message, final Object source, final Throwable cause) {
        super("Fatal implementation error in or by using " + source + ": " + message, cause);
        ExceptionPrinter.printHistory(this, LoggerFactory.getLogger(detectClass(source).getClass()));
        assert false;
    }

    /**
     * Constructor creates an new FatalImplementationErrorException and prints the exception stack via the jul exception printer. *
     *
     * Note: In case assertions are enabled this instantiation directly results in an assertion exception.
     *
     * @param cause the exception which causes the fatal implementation error.
     * @param source the instance where the error has been occurred.
     */
    public FatalImplementationErrorException(final Object source, final Throwable cause) {
        super("Fatal implementation error in or by using " + source + "!", cause);
        ExceptionPrinter.printHistory(this, LoggerFactory.getLogger(detectClass(source).getClass()));
        assert false;
    }

    /**
     * Method detects the class of the given instance. In case the instance itself is the class these one is directly returned.
     *
     * @param object
     * @return
     */
    private Class detectClass(final Object object) {
        if (object instanceof Class) {
            return (Class) object;
        }
        return object.getClass();
    }
}
