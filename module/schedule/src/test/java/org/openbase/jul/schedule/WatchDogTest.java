package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
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

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Activatable;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class WatchDogTest {

    public WatchDogTest() {
    }

    @BeforeAll
    @Timeout(30)
    public static void setUpClass() throws JPServiceException {
        JPService.setupJUnitTestMode();
    }

    /**
     * Test of activate method, of class WatchDog.
     *
     * @throws java.lang.Exception
     */
    @Timeout(5)
    @Test
    public void testActivate() throws Exception {
        System.out.println("isActive");
        WatchDog instance = new WatchDog(new TestService(), "TestService");
        instance.activate();
        assertTrue(instance.isActive());
    }

    /**
     * Test of deactivate method, of class WatchDog.
     *
     * @throws java.lang.Exception
     */
    @Timeout(5)
    @Test
    public void testDeactivate() throws Exception {
        System.out.println("deactivate");
        WatchDog instance = new WatchDog(new TestService(), "TestService");
        instance.activate();
        instance.deactivate();
        assertFalse(instance.isActive());
    }

    /**
     * Test of isActive method, of class WatchDog.
     *
     * @throws java.lang.Exception
     */
    @Timeout(5)
    @Test
    public void testIsActive() throws Exception {
        System.out.println("isActive");
        WatchDog instance = new WatchDog(new TestService(), "TestService");
        assertFalse(instance.isActive());
        instance.activate();
        assertTrue(instance.isActive());
        instance.deactivate();
        assertFalse(instance.isActive());
    }

    /**
     * Test of service error handling.
     *
     * @throws java.lang.Exception
     */
    @Timeout(5)
    @Test
    public void testServiceErrorHandling() throws Exception {
        System.out.println("serviceErrorHandling");
        WatchDog instance = new WatchDog(new TestService(), "TestService");
        assertFalse(instance.isActive());
        instance.activate();
        assertTrue(instance.isActive());
        instance.deactivate();
        assertFalse(instance.isActive());
    }

    /**
     * Test of service deactivation if never active.
     *
     * @throws java.lang.Exception
     */
    @Timeout(5)
    @Test
    public void testDeactivationInNonActiveState() throws Exception {
        System.out.println("testDeactivationInNonActiveState");
        final WatchDog watchDog = new WatchDog(new TestBadService(), "TestBadService");
        assertFalse(watchDog.isActive());

        Thread disableTask = new Thread(() -> {
            ExceptionPrinter.setBeQuit(true);

            try {
                Thread.sleep(50);
                watchDog.deactivate();
                assertFalse(watchDog.isActive());
            } catch (InterruptedException ignored) {
            }
            ExceptionPrinter.setBeQuit(false);
        });
        disableTask.start();
        watchDog.activate();
        disableTask.join();
        assertFalse(watchDog.isActive());
    }

    class TestService implements Activatable {

        private boolean active;

        @Override
        public void activate() throws CouldNotPerformException {
            active = true;
        }

        @Override
        public void deactivate() throws CouldNotPerformException, InterruptedException {
            active = false;
        }

        @Override
        public boolean isActive() {
            return active;
        }
    }

    class TestBadService implements Activatable {

        @Override
        public void activate() throws CouldNotPerformException {
            throw new NullPointerException("Simulate internal Nullpointer...");
        }

        @Override
        public void deactivate() throws CouldNotPerformException, InterruptedException {
        }

        @Override
        public boolean isActive() {
            return false;
        }
    }
}
