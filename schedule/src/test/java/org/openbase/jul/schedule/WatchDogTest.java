package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import org.openbase.jul.iface.Activatable;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author mpohling
 */
public class WatchDogTest {

	public WatchDogTest() {
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
	 * Test of activate method, of class WatchDog.
     * @throws java.lang.Exception
	 */
	@Test(timeout = 5000)
	public void testActivate() throws Exception {
		System.out.println("isActive");
		WatchDog instance = new WatchDog(new TestService(), "TestService");
		boolean expResult = true;
		instance.activate();
		boolean result = instance.isActive();
		assertEquals(expResult, result);
	}

	/**
	 * Test of deactivate method, of class WatchDog.
     * @throws java.lang.Exception
	 */
	@Test(timeout = 5000)
	public void testDeactivate() throws Exception {
		System.out.println("deactivate");
		WatchDog instance = new WatchDog(new TestService(), "TestService");
		boolean expResult = false;
		instance.activate();
		instance.deactivate();
		boolean result = instance.isActive();
		assertEquals(expResult, result);
	}

	/**
	 * Test of isActive method, of class WatchDog.
     * @throws java.lang.Exception
	 */
	@Test(timeout = 5000)
	public void testIsActive() throws Exception {
		System.out.println("isActive");
		WatchDog instance = new WatchDog(new TestService(), "TestService");
		assertEquals(instance.isActive(), false);
		instance.activate();
		assertEquals(instance.isActive(), true);
		instance.deactivate();
		assertEquals(instance.isActive(), false);
	}

	/**
	 * Test of service error handling.
     * @throws java.lang.Exception
	 */
	@Test(timeout = 5000)
	public void testServiceErrorHandling() throws Exception {
		System.out.println("serviceErrorHandling");
		WatchDog instance = new WatchDog(new TestService(), "TestService");
		assertEquals(instance.isActive(), false);
		instance.activate();
		assertEquals(instance.isActive(), true);
		instance.deactivate();
		assertEquals(instance.isActive(), false);
	}

	/**
	 * Test of service deactivation if never active.
     * @throws java.lang.Exception
	 */
	@Test(timeout = 3000)
	public void testDeactivationInNonActiveState() throws Exception {
		System.out.println("testDeactivationInNonActiveState");
		final WatchDog instance = new WatchDog(new TestBadService(), "TestBadService");
		assertEquals(instance.isActive(), false);

		new Thread() {

			@Override
			public void run() {
				try {
					Thread.sleep(500);
					instance.deactivate();
					assertEquals(false, instance.isActive());
				} catch (InterruptedException ex) {
				}
			}
		}.start();

		instance.activate();
		assertEquals(false, instance.isActive());
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

		private boolean active;

		@Override
		public void activate() throws CouldNotPerformException {
			throw new NullPointerException("Simulate internal Nullpointer...");
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
}
