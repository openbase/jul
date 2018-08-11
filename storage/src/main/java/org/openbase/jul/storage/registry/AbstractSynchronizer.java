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

import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.VerificationFailedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.IdentifiableValueMap;
import org.openbase.jul.extension.protobuf.ListDiffImpl;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.RecurrenceEventFilter;
import org.openbase.jul.schedule.Stopwatch;
import org.openbase.jul.schedule.SyncObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractSynchronizer<KEY, ENTRY extends Identifiable<KEY>> implements Activatable, Shutdownable {

    public static final long DEFAULT_MAX_FREQUENCY = 15000;

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final IdentifiableValueMap<KEY, ENTRY> currentEntryMap;
    private final ListDiffImpl<KEY, ENTRY> listDiff;
    private final DataProvider dataProvider;
    private boolean isActive = false;
    private final Observer observer;
    private final RecurrenceEventFilter recurrenceSyncFilter;

    private boolean initialSync;

    protected final SyncObject synchronizationLock;

    public AbstractSynchronizer(final DataProvider dataProvider) throws org.openbase.jul.exception.InstantiationException {
        this(dataProvider, DEFAULT_MAX_FREQUENCY);
    }

    public AbstractSynchronizer(final DataProvider dataProvider, final SyncObject synchronizationLock) throws org.openbase.jul.exception.InstantiationException {
        this(dataProvider, DEFAULT_MAX_FREQUENCY, synchronizationLock);
    }

    public AbstractSynchronizer(final DataProvider dataProvider, final long maxFrequency) throws org.openbase.jul.exception.InstantiationException {
        this(dataProvider, maxFrequency, new SyncObject("SynchronizationLock"));
    }

    public AbstractSynchronizer(final DataProvider dataProvider, final long maxFrequency, final SyncObject synchronizationLock) throws org.openbase.jul.exception.InstantiationException {
        try {
            this.synchronizationLock = synchronizationLock;
            this.initialSync = true;
            this.listDiff = new ListDiffImpl<>();
            this.dataProvider = dataProvider;
            this.currentEntryMap = new IdentifiableValueMap<>();
            this.recurrenceSyncFilter = new RecurrenceEventFilter(maxFrequency) {
                @Override
                public void relay() throws Exception {
                    // skip relay if synchronizer is not active.
                    if (!isActive()) {
                        return;
                    }

                    internalSync();
                }
            };
            this.observer = (Observable source, Object data) -> {
                logger.debug("Incoming update...");
                GlobalCachedExecutorService.submit(() -> {
                    try {
                        recurrenceSyncFilter.trigger();
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory("Could not trigger synchronization", ex, logger);
                    }
                });
            };
        } catch (Exception ex) {
            throw new org.openbase.jul.exception.InstantiationException(this, ex);
        }
    }

    @Override
    public void activate() throws CouldNotPerformException, InterruptedException {
        // add data observer
        dataProvider.addDataObserver(observer);

        try {
            // trigger internal sync if data is available.
            if (dataProvider.isDataAvailable()) {
                internalSync();
            }
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial sync failed!", ex), logger, LogLevel.ERROR);
        }
        isActive = true;
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        isActive = false;

        dataProvider.removeDataObserver(observer);
        recurrenceSyncFilter.cancel();
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public void shutdown() {
        try {
            deactivate();
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not shutdown " + this, ex, logger);
        }
    }

    private void internalSync() throws CouldNotPerformException, InterruptedException {
        synchronized (synchronizationLock) {
            logger.warn("Perform sync...");
            Stopwatch stopwatch = new Stopwatch();
            stopwatch.start();

            try {
                listDiff.diff(getEntries());
                int skippedChanges = 0;

                MultiException.ExceptionStack removeExceptionStack = null;
                for (ENTRY entry : listDiff.getRemovedValueMap().values()) {
                    try {
                        removeInternal(entry);
                    } catch (CouldNotPerformException ex) {
                        removeExceptionStack = MultiException.push(this, ex, removeExceptionStack);
                    }
                }

                MultiException.ExceptionStack updateExceptionStack = null;
                for (ENTRY entry : listDiff.getUpdatedValueMap().values()) {
                    try {
                        if (verifyEntry(entry)) {
                            updateInternal(entry);
                        } else {
                            removeInternal(entry);
                            listDiff.getOriginalValueMap().removeValue(entry);
                        }
                    } catch (CouldNotPerformException ex) {
                        updateExceptionStack = MultiException.push(this, ex, updateExceptionStack);
                    }
                }

                MultiException.ExceptionStack registerExceptionStack = null;
                for (ENTRY entry : listDiff.getNewValueMap().values()) {
                    try {
                        if (verifyEntry(entry)) {
                            registerInternal(entry);
                        } else {
                            skippedChanges++;
                        }
                    } catch (CouldNotPerformException ex) {
                        registerExceptionStack = MultiException.push(this, ex, registerExceptionStack);
                    }
                }

                // print changes
                final int errorCounter = MultiException.size(removeExceptionStack) + MultiException.size(updateExceptionStack) + MultiException.size(registerExceptionStack);
                final int changeCounter = (listDiff.getChangeCounter() - skippedChanges);
                if (changeCounter != 0 || errorCounter != 0) {
                    logger.info(changeCounter + " changes synchronized." + (errorCounter == 0 ? "" : " " + errorCounter + (errorCounter == 1 ? " is" : " are") + " skipped."));
                }

                // sync list diff to what actually happened
                listDiff.replaceOriginalMap(currentEntryMap);

                // build exception cause chain.
                MultiException.ExceptionStack exceptionStack = null;
                int counter;
                try {
                    if (removeExceptionStack != null) {
                        counter = removeExceptionStack.size();
                    } else {
                        counter = 0;
                    }
                    MultiException.checkAndThrow("Could not remove " + counter + " entries!", removeExceptionStack);
                } catch (CouldNotPerformException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
                try {
                    if (updateExceptionStack != null) {
                        counter = updateExceptionStack.size();
                    } else {
                        counter = 0;
                    }
                    MultiException.checkAndThrow("Could not update " + counter + " entries!", updateExceptionStack);
                } catch (CouldNotPerformException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
                try {
                    if (registerExceptionStack != null) {
                        counter = registerExceptionStack.size();
                    } else {
                        counter = 0;
                    }
                    MultiException.checkAndThrow("Could not register " + counter + " entries!", registerExceptionStack);
                } catch (CouldNotPerformException ex) {
                    exceptionStack = MultiException.push(this, ex, exceptionStack);
                }
                MultiException.checkAndThrow("Could not sync all entries!", exceptionStack);
            } catch (CouldNotPerformException ex) {
                CouldNotPerformException exx = new CouldNotPerformException("Entry registry sync failed!", ex);
                if (JPService.testMode()) {
                    ExceptionPrinter.printHistory(exx, logger);
                    assert false; // exit if errors occurs during unit tests.
                }
                throw exx;
            } finally {
                afterInternalSync();
                initialSync = false;
            }

            long time = stopwatch.stop();
            if (time > 500) {
                logger.warn("Internal sync of synchronizer took: " + time + "ms");
            }
        }
    }

    private void updateInternal(final ENTRY entry) throws CouldNotPerformException, InterruptedException {
        update(entry);
        this.currentEntryMap.put(entry);
    }

    private void registerInternal(final ENTRY entry) throws CouldNotPerformException, InterruptedException {
        register(entry);
        this.currentEntryMap.put(entry);
    }

    private void removeInternal(final ENTRY entry) throws CouldNotPerformException, InterruptedException {
        remove(entry);
        this.currentEntryMap.removeValue(entry);
    }

    public abstract void update(final ENTRY entry) throws CouldNotPerformException, InterruptedException;

    public abstract void register(final ENTRY entry) throws CouldNotPerformException, InterruptedException;

    public abstract void remove(final ENTRY entry) throws CouldNotPerformException, InterruptedException;

    public abstract List<ENTRY> getEntries() throws CouldNotPerformException;

    /**
     * Method should return true if the given entry is valid, otherwise
     * false. This default implementation accepts all entries. To
     * implement a custom verification just overwrite this method.
     *
     * @param entry the entry which is tested
     * @return if the entry should be synchronized
     * @throws org.openbase.jul.exception.VerificationFailedException if verifying the entry fails
     */
    public abstract boolean verifyEntry(final ENTRY entry) throws VerificationFailedException;

    protected void afterInternalSync() {
    }

    /**
     * Get if the a sync is the initial sync. This can be used by implementations to do something different on the
     * initial sync because in the initial sync entries will only be registered.
     *
     * @return true if the initial sync has not yet been performed.
     */
    protected boolean isInitialSync() {
        return initialSync;
    }

    protected DataProvider getDataProvider() {
        return dataProvider;
    }
}
