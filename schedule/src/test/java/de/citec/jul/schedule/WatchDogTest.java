/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.iface.Activatable;
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
	 */
	@Test
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
	 */
	@Test
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
	 */
	@Test
	public void testIsActive() throws CouldNotPerformException, InterruptedException, InstantiationException {
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
	 */
	@Test
	public void testServiceErrorHandling() throws CouldNotPerformException, InterruptedException, InstantiationException {
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
	 */
	@Test(timeout = 3000)
	public void testDeactivationInNonActiveState() throws CouldNotPerformException, InterruptedException, InstantiationException {
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
					Logger.getLogger(WatchDogTest.class.getName()).log(Level.SEVERE, null, ex);
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
			throw new NullPointerException("Could not activate, simulate internal Nullpointer...");
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
