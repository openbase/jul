package org.openbase.jul.extension.protobuf;

/*-
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import com.google.protobuf.Message;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.AfterClass;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.Stopwatch;
import rst.domotic.state.BrightnessStateType.BrightnessState;
import rst.domotic.state.ColorStateType.ColorState;
import rst.domotic.state.MotionStateType.MotionState;
import rst.domotic.state.PowerStateType.PowerState;
import rst.domotic.state.PresenceStateType.PresenceState;
import rst.domotic.unit.dal.ColorableLightDataType.ColorableLightData;
import rst.domotic.unit.location.LocationDataType.LocationData;
import rst.timing.TimestampType.Timestamp;
import rst.vision.ColorType.Color;
import rst.vision.HSBColorType.HSBColor;

/**
 *
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class MessageObservableTest {

    public MessageObservableTest() {
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
     * Test of computeHash method, of class MessageObservable.
     *
     * @throws java.lang.Exception
     */
    @Test
    public void testComputeHash() throws Exception {
        System.out.println("computeHash");

        final Timestamp timestamp1 = Timestamp.newBuilder().setTime(20).build();
        final Timestamp timestamp2 = Timestamp.newBuilder().setTime(100).build();

        final PowerState powerState1 = PowerState.newBuilder().setTimestamp(timestamp1).setValue(PowerState.State.ON).build();
        final PowerState powerState2 = PowerState.newBuilder().setTimestamp(timestamp2).setValue(PowerState.State.ON).build();

        final ColorableLightData colorableLightData1 = ColorableLightData.newBuilder().setPowerState(powerState1).build();
        final ColorableLightData colorableLightData2 = ColorableLightData.newBuilder().setPowerState(powerState2).build();

        final MessageObservable<ColorableLightData> messageObservable = new MessageObservableImpl<>(ColorableLightData.class);

        assertEquals("Hashes are not equal even though only the timestamp has changed", messageObservable.removeTimestamps(colorableLightData1.toBuilder()).build().hashCode(), messageObservable.removeTimestamps(colorableLightData2.toBuilder()).build().hashCode());

        messageObservable.addObserver((Observable<ColorableLightData> source, ColorableLightData data) -> {
            assertEquals("Received unexpected update", colorableLightData1, data);
        });

        messageObservable.notifyObservers(colorableLightData1);
        messageObservable.notifyObservers(colorableLightData2);

        Stopwatch stopwatch = new Stopwatch();
        stopwatch.start();
        for (int i = 0; i < 1000; ++i) {
            messageObservable.removeTimestamps(colorableLightData1.toBuilder()).build().hashCode();
        }
        stopwatch.stop();
        System.out.println("Computing 1000 hashes took " + stopwatch.getTime() + "ms");
    }

    @Test
    public void testForNestedType() throws Exception {
        System.out.println("testForNestedType");

        final MessageObservable<LocationData> messageObservable = new MessageObservableImpl<>(LocationData.class);

        final LocationData.Builder locationData1 = LocationData.newBuilder();
        final PowerState.Builder powerState1 = locationData1.getPowerStateBuilder();
        final BrightnessState.Builder brightnessState1 = locationData1.getBrightnessStateBuilder();
        final MotionState.Builder motionState1 = locationData1.getMotionStateBuilder();
        final ColorState.Builder colorState1 = locationData1.getColorStateBuilder();
        final PresenceState.Builder presenceState1 = locationData1.getPresenceStateBuilder();
        powerState1.setValue(PowerState.State.ON).setTimestamp(Timestamp.newBuilder().setTime(100));
        brightnessState1.setBrightness(65).setTimestamp(Timestamp.newBuilder().setTime(200));
        motionState1.setValue(MotionState.State.MOTION).setTimestamp(Timestamp.newBuilder().setTime(300));
        colorState1.setTimestamp(Timestamp.newBuilder().setTime(400));
        final Color.Builder color1 = colorState1.getColorBuilder();
        color1.setType(Color.Type.HSB);
        final HSBColor.Builder hsbColor1 = color1.getHsbColorBuilder();
        hsbColor1.setBrightness(10).setHue(20).setSaturation(30);
        presenceState1.setLastPresence(Timestamp.newBuilder().setTime(500)).setTimestamp(Timestamp.newBuilder().setTime(500)).setValue(PresenceState.State.ABSENT);

        final LocationData.Builder locationData2 = LocationData.newBuilder();
        final PowerState.Builder powerState2 = locationData2.getPowerStateBuilder();
        final BrightnessState.Builder brightnessState2 = locationData2.getBrightnessStateBuilder();
        final MotionState.Builder motionState2 = locationData2.getMotionStateBuilder();
        final ColorState.Builder colorState2 = locationData2.getColorStateBuilder();
        final PresenceState.Builder presenceState2 = locationData2.getPresenceStateBuilder();
        powerState2.setValue(PowerState.State.ON).setTimestamp(Timestamp.newBuilder().setTime(12));
        brightnessState2.setBrightness(65).setTimestamp(Timestamp.newBuilder().setTime(15));
        motionState2.setValue(MotionState.State.MOTION).setTimestamp(Timestamp.newBuilder().setTime(62));
        colorState2.setTimestamp(Timestamp.newBuilder().setTime(152));
        final Color.Builder color2 = colorState2.getColorBuilder();
        color2.setType(Color.Type.HSB);
        final HSBColor.Builder hsbColor2 = color2.getHsbColorBuilder();
        hsbColor2.setBrightness(10).setHue(20).setSaturation(30);
        presenceState2.setLastPresence(Timestamp.newBuilder().setTime(1231)).setTimestamp(Timestamp.newBuilder().setTime(1231)).setValue(PresenceState.State.ABSENT);

        messageObservable.addObserver(new Observer<LocationData>() {

            private int notificationCounter = 0;

            @Override
            public void update(Observable<LocationData> source, LocationData data) throws Exception {
                notificationCounter++;
                assertEquals("LocationData has been notified even though only the timestamp changed", 1, notificationCounter);
            }
        });

        assertFalse("LocationData equal even though they have different timestamps", locationData1.build().equals(locationData2.build()));
        assertFalse("LocationData hashcodes are equal even though they have different timestamps", locationData1.build().hashCode() == locationData2.build().hashCode());
        assertEquals("Hashes of both data types do not match after removed timestamps", messageObservable.removeTimestamps(locationData1).build().hashCode(), messageObservable.removeTimestamps(locationData2).build().hashCode());

        messageObservable.notifyObservers(locationData1.build());
        messageObservable.notifyObservers(locationData2.build());
    }

    public class MessageObservableImpl<M extends Message> extends MessageObservable<M> {

        public MessageObservableImpl(final Class<M> dataClass) {
            super(new DataProviderImpl<>(dataClass));
        }
    }

    public class DataProviderImpl<M extends Message> implements DataProvider<M> {

        private final Class<M> dataClass;

        public DataProviderImpl(final Class<M> dataClass) {
            this.dataClass = dataClass;
        }

        @Override
        public boolean isDataAvailable() {
            return true;
        }

        @Override
        public Class<M> getDataClass() {
            return dataClass;
        }

        @Override
        public M getData() throws NotAvailableException {
            return null;
        }

        @Override
        public CompletableFuture<M> getDataFuture() {
            return CompletableFuture.completedFuture(null);
        }

        @Override
        public void addDataObserver(Observer<M> observer) {
        }

        @Override
        public void removeDataObserver(Observer<M> observer) {
        }

        @Override
        public void waitForData() throws CouldNotPerformException, InterruptedException {
        }

        @Override
        public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        }
    }
}
