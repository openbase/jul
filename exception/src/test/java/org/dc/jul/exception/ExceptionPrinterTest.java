package org.dc.jul.exception;

/*
 * #%L
 * JUL Exception
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.MultiException.ExceptionStack;
import org.dc.jul.exception.printer.LogLevel;
import java.util.logging.Level;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public class ExceptionPrinterTest {

    public ExceptionPrinterTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    /**
     * Test of printHistory method, of class ExceptionPrinter.
     */
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

        Exception baseException1 = new CouldNotPerformException("Base Exception", ex7);
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
            MultiException.checkAndThrow("Multi Exception 1", stack);
        } catch (MultiException ex) {
            stack2 = MultiException.push(this, ex, stack2);
            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
        }

        stack2 = MultiException.push(this, ex1, stack2);
        stack2 = MultiException.push(this, ex2, stack2);
        stack2 = MultiException.push(this, ex3, stack2);
        stack2 = MultiException.push(this, ex4, stack2);

        try {
            MultiException.checkAndThrow("Multi Exception 2", stack2);
        } catch (MultiException ex) {
            ExceptionPrinter.printHistory(ex, logger, LogLevel.ERROR);
            System.out.println("Test getHistory:");
            System.out.println(ExceptionPrinter.getHistory(ex));
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
