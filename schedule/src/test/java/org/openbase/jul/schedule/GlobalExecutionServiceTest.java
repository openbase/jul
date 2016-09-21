/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.schedule;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public class GlobalExecutionServiceTest {

    public GlobalExecutionServiceTest() {
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
     * Test of getInstance method, of class GlobalExecutionService.
     */
    @Test
    public void testShutdown() throws Exception {
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                int i = 0;
                while (true) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        i++;
                        System.out.println("I = " + i);
                    }
                }
            }
        };

        GlobalExecutionService.execute(runnable);
    }

}
