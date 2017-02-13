package org.openbase.jul.extension.rst.processing;

import com.google.protobuf.GeneratedMessage;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import rst.domotic.state.PowerStateType;
import rst.timing.TimestampType;

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
        long time2 = TimestampJavaTimeTransform.transform(TimestampProcessor.updateTimeStampWithCurrentTime(powerState).getTimestamp());
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
        TimestampProcessor.updateTimeStamp(time, powerState);
        assertEquals(TimestampJavaTimeTransform.transform(time), powerState.getTimestamp());
    }
}
