package org.openbase.jul.extension.protobuf;

/*-
 * #%L
 * JUL Extension Protobuf
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

import com.google.protobuf.Message;
import org.junit.jupiter.api.Test;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.FutureProcessor;
import org.openbase.jul.schedule.Stopwatch;
import org.openbase.type.domotic.state.PowerStateType.PowerState;
import org.openbase.type.domotic.unit.dal.ColorableLightDataType.ColorableLightData;
import org.openbase.type.timing.TimestampType.Timestamp;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * @author <a href="mailto:pleminoq@openbase.org">Tamino Huxohl</a>
 */
public class MessageObservableTest {

    public MessageObservableTest() {
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

        assertEquals(
                messageObservable.removeTimestamps(colorableLightData1.toBuilder()).build().hashCode(),
                messageObservable.removeTimestamps(colorableLightData2.toBuilder()).build().hashCode(),
                "Hashes are not equal even though only the timestamp has changed");

        messageObservable.addObserver((DataProvider<ColorableLightData> source, ColorableLightData data) -> {
            assertEquals(
                    colorableLightData1,
                    data,
                    "Received unexpected update");
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
        public M getData() {
            return null;
        }

        @Override
        public Future<M> getDataFuture() {
            return FutureProcessor.completedFuture(getData());
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

        @Override
        public void validateData() throws InvalidStateException {

        }
    }
}
