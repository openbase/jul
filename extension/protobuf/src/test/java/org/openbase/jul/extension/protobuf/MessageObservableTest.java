package org.openbase.jul.extension.protobuf;

/*-
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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

import com.google.protobuf.Descriptors.Descriptor;
import com.google.protobuf.Descriptors.EnumDescriptor;
import com.google.protobuf.Descriptors.EnumValueDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor;
import com.google.protobuf.Descriptors.FieldDescriptor.JavaType;
import com.google.protobuf.Message;
import com.google.protobuf.Message;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.sql.Time;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import com.google.protobuf.MessageOrBuilder;
import com.google.protobuf.ProtocolMessageEnum;
import org.junit.After;
import org.junit.AfterClass;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.extension.protobuf.processing.ProtoBufFieldProcessor;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.Stopwatch;
import org.openbase.type.domotic.state.BrightnessStateType.BrightnessState;
import org.openbase.type.domotic.state.ColorStateType.ColorState;
import org.openbase.type.domotic.state.ContactStateType.ContactState;
import org.openbase.type.domotic.state.MotionStateType.MotionState;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.state.PresenceStateType.PresenceState;
import org.openbase.type.domotic.state.PresenceStateType.PresenceState.MapFieldEntry;
import org.openbase.type.domotic.state.PresenceStateType.PresenceState.State;
import org.openbase.type.domotic.unit.dal.ColorableLightDataType.ColorableLightData;
import org.openbase.type.domotic.unit.location.LocationDataType.LocationData;
import org.openbase.type.timing.TimestampType.Timestamp;
import org.openbase.type.timing.TimestampType.Timestamp.Builder;
import org.openbase.type.timing.TimestampType.TimestampOrBuilder;
import org.openbase.type.vision.ColorType.Color;
import org.openbase.type.vision.HSBColorType.HSBColor;

/**
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

        final MessageObservable<DataProvider<ColorableLightData>, ColorableLightData> messageObservable = new MessageObservableImpl<>(ColorableLightData.class);

        assertEquals("Hashes are not equal even though only the timestamp has changed", messageObservable.removeTimestamps(colorableLightData1.toBuilder()).build().hashCode(), messageObservable.removeTimestamps(colorableLightData2.toBuilder()).build().hashCode());

        messageObservable.addObserver((DataProvider<ColorableLightData> source, ColorableLightData data) -> {
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

    public class MessageObservableImpl<M extends Message> extends MessageObservable<DataProvider<M>, M> {

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
        public void addDataObserver(Observer<DataProvider<M>, M> observer) {
        }

        @Override
        public void removeDataObserver(Observer<DataProvider<M>, M> observer) {
        }

        @Override
        public void waitForData() throws CouldNotPerformException, InterruptedException {
        }

        @Override
        public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        }
    }
}
