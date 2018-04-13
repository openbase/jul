package org.openbase.jul.storage.registry;

/*-
 * #%L
 * JUL Storage
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.pattern.AbstractObservable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.GlobalScheduledExecutorService;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public abstract class AbstractScheduledSynchronizer<KEY, ENTRY extends Identifiable<KEY>> extends AbstractSynchronizer<KEY, ENTRY> {
    public AbstractScheduledSynchronizer(DataProvider observable) throws InstantiationException {
        super(observable);
    }

//    public AbstractScheduledSynchronizer(final long initialDelay, final long period, final TimeUnit timeUnit) {
//        super(new AbstractObservable(false) {
//
//            @Override
//            public void waitForValue(long timeout, TimeUnit timeUnit) {
//                return;
//            }
//
//            @Override
//            public Object getValue() {
//                return null;
//            }
//
//            @Override
//            public Future getValueFuture() {
//                return null;
//            }
//
//            @Override
//            public boolean isValueAvailable() {
//                return false;
//            }
//        });
//    }
//
//    public static class ScheduledDataProvider implements DataProvider {
//
//        private final Runnable notifier;
//        private final long initialDelay;
//        private final long period;
//        private final TimeUnit timeUnit;
//        private final Observable observable;
//
//        private ScheduledFuture scheduledFuture = null;
//
//        public ScheduledDataProvider(final long initialDelay, final long period, final TimeUnit timeUnit) {
//            this.notifier = () -> {
//                try {
//                    notifyObservers("");
//                } catch (CouldNotPerformException ex) {
////                    ExceptionPrinter.printHistory(ex, logger);
//                }
//            };
//            this.initialDelay = initialDelay;
//            this.period = period;
//            this.timeUnit = timeUnit;
//        }
//
//        @Override
//        public void shutdown() {
//            super.shutdown();
//
//            if (scheduledFuture != null) {
//                scheduledFuture.cancel(true);
//            }
//        }
//
//        public void activate() {
//            try {
//                scheduledFuture = GlobalScheduledExecutorService.scheduleAtFixedRate(notifier, initialDelay, period, timeUnit);
//            } catch (NotAvailableException ex) {
//                // is only thrown if runnable is null which is not the case here
//                new FatalImplementationErrorException(this, ex);
//            }
//        }
//
//        @Override
//        public boolean isDataAvailable() {
//            return false;
//        }
//
//        @Override
//        public Class getDataClass() {
//            return null;
//        }
//
//        @Override
//        public Object getData() throws NotAvailableException {
//            return null;
//        }
//
//        @Override
//        public CompletableFuture getDataFuture() {
//            return null;
//        }
//
//        @Override
//        public void addDataObserver(Observer observer) {
//
//        }
//
//        @Override
//        public void removeDataObserver(Observer observer) {
//
//        }
//
//        @Override
//        public void waitForData() throws CouldNotPerformException, InterruptedException {
//
//        }
//
//        @Override
//        public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
//
//        }
//    }
}
