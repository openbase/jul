package de.citec.jul.exception;

import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.MultiException.ExceptionStack;
import de.citec.jul.exception.printer.LogLevel;
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
