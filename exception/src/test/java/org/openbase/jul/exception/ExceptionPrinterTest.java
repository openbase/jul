package org.openbase.jul.exception;

/*
 * #%L
 * JUL Exception
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.MultiException.ExceptionStack;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ExceptionPrinterTest {

    public ExceptionPrinterTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of printHistory method, of class ExceptionPrinter.
     */
    @Timeout(5)
    @Test
    public void testPrintHistory() {
        System.out.println("printHistory");
        Logger logger = LoggerFactory.getLogger(ExceptionPrinterTest.class);

        ExceptionStack stack = null;
        ExceptionStack stack2 = null;

        Exception ex1 = new CouldNotPerformException("No Way 1", new NullPointerException());
        Exception ex2 = new CouldNotPerformException("No Way 2", ex1);
        Exception ex3 = new CouldNotPerformException("No Way 3", ex2);
        Exception ex4 = new CouldNotPerformException("No Way 4", ex3);
        Exception ex5 = new CouldNotPerformException("No Way 5", ex4);
        Exception ex6 = new CouldNotPerformException("No Way 6", ex5);
        Exception ex7 = new CouldNotPerformException("No Way 7", ex6);

        Exception baseException1 = new CouldNotPerformException("BaseException", ex7);
        stack = MultiException.push(this, ex1, stack);
        stack = MultiException.push(this, ex2, stack);
        stack = MultiException.push(this, ex3, stack);
        stack = MultiException.push(this, ex4, stack);
        stack = MultiException.push(this, ex5, stack);
        stack = MultiException.push(this, ex6, stack);
        stack = MultiException.push(this, ex7, stack);
        stack = MultiException.push(this, baseException1, stack);

        ExceptionPrinter.printHistory(baseException1, logger, LogLevel.ERROR);

        stack2 = MultiException.push(this, ex5, stack2);
        stack2 = MultiException.push(this, ex6, stack2);
        stack2 = MultiException.push(this, ex7, stack2);
        stack2 = MultiException.push(this, baseException1, stack2);

        try {
            MultiException.checkAndThrow(() -> "MultiException 1", stack);
        } catch (MultiException ex) {
            stack2 = MultiException.push(this, ex, stack2);
            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
        }

        stack2 = MultiException.push(this, ex1, stack2);
        stack2 = MultiException.push(this, ex2, stack2);
        stack2 = MultiException.push(this, ex3, stack2);
        stack2 = MultiException.push(this, ex4, stack2);

        try {
            MultiException.checkAndThrow(() -> "MultiException 2", stack2);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
            ExceptionPrinter.printHistory(new CouldNotPerformException("BaseException containing MultiException", ex), logger);
            ExceptionPrinter.printHistory(new CouldNotPerformException("BaseBaseException", new CouldNotPerformException("BaseException containing MultiException", ex)), logger);
            ExceptionPrinter.printHistory(new CouldNotPerformException("BaseBaseBaseException", new CouldNotPerformException("BaseBaseException", new CouldNotPerformException("BaseException containing MultiException", ex))), logger);
        }

    }

    /**
     * Test of printHistory method, of class ExceptionPrinter.
     */
    @Timeout(5)
    @Test
    public void testPrintMultiExceptionHistory() {
        System.out.println("printHistory");
        Logger logger = LoggerFactory.getLogger(ExceptionPrinterTest.class);

        ExceptionStack innerStack = null;
        ExceptionStack outerStack = null;

        Exception ex1 = new CouldNotPerformException("No Way 1", new NullPointerException());
        Exception ex2 = new CouldNotPerformException("No Way 2", new NullPointerException());
        Exception ex3 = new CouldNotPerformException("No Way 3", new NullPointerException());
        Exception ex4 = new CouldNotPerformException("No Way 4", new NullPointerException());
        Exception ex5 = new CouldNotPerformException("No Way 5", new NullPointerException());
        Exception ex6 = new CouldNotPerformException("No Way 6", new NullPointerException());
        Exception ex7 = new CouldNotPerformException("No Way 7", new NullPointerException());

        innerStack = MultiException.push(this, ex1, innerStack);
        innerStack = MultiException.push(this, ex2, innerStack);
        innerStack = MultiException.push(this, ex3, innerStack);
        innerStack = MultiException.push(this, ex4, innerStack);
        innerStack = MultiException.push(this, ex5, innerStack);
        innerStack = MultiException.push(this, ex6, innerStack);
        innerStack = MultiException.push(this, ex7, innerStack);

        try {
            MultiException.checkAndThrow(() -> "InnerMultiException", innerStack);
        } catch (MultiException ex) {
            outerStack = MultiException.push(this, new NotAvailableException("first"), outerStack);
            outerStack = MultiException.push(this, ex, outerStack);
            outerStack = MultiException.push(this, new NotAvailableException("third"), outerStack);
        }

        try {
            MultiException.checkAndThrow(() -> "OuterMultiException", outerStack);
        } catch (MultiException ex) {
//            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
            ExceptionPrinter.printHistory(new CouldNotPerformException("Base 1", ex), logger, LogLevel.ERROR);
        }

    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
