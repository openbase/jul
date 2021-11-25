package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
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
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TimeoutTest {

    public TimeoutTest() {
    }

    @BeforeAll
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of getTimeToWait method, of class Timeout.
     */
    @org.junit.jupiter.api.Timeout(3)
    @Test
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
        assertTrue(timeout.isActive(), "timer was started but is not active!");
        stopwatch.start();
        stopwatch.waitForStop();
        System.out.println("time: " + stopwatch.getTime());
        assertTrue(Math.abs(stopwatch.getTime() - timeToWait) < 50, "timer to fast!");

        // #### Test timeout cancel ####
        stopwatch.reset();

        timeout.start(50);
        Thread.sleep(10);
        timeout.cancel();
        Thread.sleep(100);
        assertTrue(!timeout.isExpired(), "Timeout expired but was canceled!");
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
