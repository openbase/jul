/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

import static de.citec.jul.schedule.RecurrenceEventFilterTest.RecurrenceEventFilterImpl.TIMEOUT;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class RecurrenceEventFilterTest {

    public RecurrenceEventFilterTest() {
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
     * Test of trigger method, of class RecurrenceEventFilter.
     */
    @Test
    public void testRecurrenceEventFilter() throws InterruptedException {
        System.out.println("trigger");
        RecurrenceEventFilterImpl instance = new RecurrenceEventFilterImpl();

        for (int i = 0; i < 100; i++) {
            instance.trigger();
        }
        Thread.sleep(TIMEOUT + 10);
        assertEquals(2, instance.getRelayCounter());
    }

    public class RecurrenceEventFilterImpl extends RecurrenceEventFilter {

        public static final long TIMEOUT = 500;

        private int relayCounter = 0;

        public RecurrenceEventFilterImpl() {
            super(TIMEOUT);
        }

        @Override
        public void relay() {
            relayCounter++;
        }

        public int getRelayCounter() {
            return relayCounter;
        }
    }
}
