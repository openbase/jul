package org.openbase.jul.extension.type.processing;

/*-
 * #%L
 * JUL Extension Type Processing
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.type.domotic.state.PowerStateType;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class TimestampProcessorTest {

    public TimestampProcessorTest() {
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
     * Test of getCurrentTimestamp method, of class TimestampProcessor.
     */
    @Test
    public void testGetCurrentTimestamp() throws InterruptedException {
        System.out.println("getCurrentTimestamp");
        long time1 = System.currentTimeMillis();
        Thread.sleep(1);
        long time2 = TimestampJavaTimeTransform.transform(TimestampProcessor.getCurrentTimestamp());
        Thread.sleep(1);
        long time3 = System.currentTimeMillis();
        assertTrue(time1 < time2);
        assertTrue(time2 < time3);
    }

    /**
     * Test of updateTimeStampWithCurrentTime method, of class TimestampProcessor.
     */
    @Test
    public void testUpdateTimeStampWithCurrentTime() throws Exception {
        System.out.println("updateTimeStampWithCurrentTime");
        PowerStateType.PowerState powerState = PowerStateType.PowerState.newBuilder().build();
        long time1 = System.currentTimeMillis();
        Thread.sleep(1);
        long time2 = TimestampJavaTimeTransform.transform(TimestampProcessor.updateTimestampWithCurrentTime(powerState).getTimestamp());
        Thread.sleep(1);
        long time3 = System.currentTimeMillis();
        assertTrue(time1 < time2);
        assertTrue(time2 < time3);

    }

    /**
     * Test of updateTimeStamp method, of class TimestampProcessor.
     */
    @Test
    public void testUpdateTimeStamp() throws Exception {
        System.out.println("updateTimeStamp");
        PowerStateType.PowerState.Builder powerState = PowerStateType.PowerState.newBuilder();
        long time = 9999;
        TimestampProcessor.updateTimestamp(time, powerState);
        assertEquals(TimestampJavaTimeTransform.transform(time), powerState.getTimestamp());
    }

    /**
     * Test id updating the timestamp also works for messages and not only builder.
     */
    @Test
    public void setTimestampForMessage() throws Exception {
        System.out.println("setTimestampForMessage");
        PowerStateType.PowerState powerState = PowerStateType.PowerState.getDefaultInstance();
        long time = 1238;
        powerState = TimestampProcessor.updateTimestamp(time, powerState);
        assertEquals(TimestampJavaTimeTransform.transform(time), powerState.getTimestamp());
    }
}