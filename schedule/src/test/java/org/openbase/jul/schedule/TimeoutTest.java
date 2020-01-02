package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TimeoutTest {

    public TimeoutTest() {
    }

    @BeforeClass
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
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
     * Test of getTimeToWait method, of class Timeout.
     */
    @Test(timeout = 3000)
    public void testTimer() throws Exception {
        System.out.println("getTimeToWait");
        final Stopwatch stopwatch = new Stopwatch();
        final long timeToWait = 200;

        // #### Test timeout expire ####
        Timeout timeout = new Timeout(timeToWait) {

            @Override
            public void expired() {
                try {
                    stopwatch.stop();
                } catch (CouldNotPerformException ex) {
                    assertTrue(false);
                }
            }
        };

        timeout.start();
        assertTrue("timer was started but is not active!", timeout.isActive());
        stopwatch.start();
        stopwatch.waitForStop();
        System.out.println("time: " + stopwatch.getTime());
        assertTrue("timer to fast!", Math.abs(stopwatch.getTime() - timeToWait) < 50);

        // #### Test timeout cancel ####
        stopwatch.reset();

        timeout.start(50);
        Thread.sleep(10);
        timeout.cancel();
        Thread.sleep(100);
        assertTrue("Timeout expired but was canceled!", !timeout.isExpired());
        try {
            stopwatch.getEndTime();
            assertTrue(false);
        } catch (CouldNotPerformException ex) {
            // there should be no result because timeout was canceled.
        }
        // #### Test multi timeout start behaviour ####

        stopwatch.reset();
        timeout.start(50);
        timeout.start(50);
        timeout.start(50);

    }
}
