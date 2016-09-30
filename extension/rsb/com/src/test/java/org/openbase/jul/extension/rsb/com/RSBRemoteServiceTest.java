package org.openbase.jul.extension.rsb.com;

/*-
 * #%L
 * JUL Extension RSB Communication
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
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jul.exception.TimeoutException;
import org.openbase.jul.pattern.Remote;

/**
 *
 * * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class RSBRemoteServiceTest {

    public RSBRemoteServiceTest() {
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
     * Test of waitForConnectionState method, of class RSBRemoteService.
     *
     * @throws java.lang.InterruptedException
     */
    @Test(timeout = 10000)
    public void testWaitForConnectionState_RemoteRemoteConnectionState_long() throws InterruptedException {
        System.out.println("waitForConnectionState");
        RSBRemoteService instance = new RSBCommunicationServiceTest.RSBRemoteServiceImpl();
        try {
            instance.waitForConnectionState(Remote.ConnectionState.CONNECTING, 10);
            Assert.fail("No exception thrown.");
        } catch (TimeoutException e) {
            // should be thrown...
            Assert.assertTrue(true);
        }
    }
}
