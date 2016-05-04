package org.dc.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import com.google.protobuf.GeneratedMessage;
import java.util.Map;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPTestMode;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.exception.VerificationFailedException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.IdentifiableMessageMap;
import org.dc.jul.extension.protobuf.ProtobufListDiff;
import org.dc.jul.iface.Configurable;
import static org.dc.jul.iface.Identifiable.TYPE_FIELD_ID;
import org.dc.jul.pattern.Factory;
import org.dc.jul.pattern.Observable;
import org.dc.jul.pattern.Observer;
import org.dc.jul.schedule.RecurrenceEventFilter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 * @param <CONFIG_M>
 * @param <CONFIG_MB>
 */
public class RegistrySynchronizer<KEY, ENTRY extends Configurable<KEY, CONFIG_M>, CONFIG_M extends GeneratedMessage, CONFIG_MB extends CONFIG_M.Builder<CONFIG_MB>> {

    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final Registry<KEY, ENTRY, ?> localRegistry;
    private final Observer<Map<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>>> remoteChangeObserver;
    private final RecurrenceEventFilter recurrenceSyncFilter;
    private final ProtobufListDiff<KEY, CONFIG_M, CONFIG_MB> entryConfigDiff;
    private final Factory<ENTRY, CONFIG_M> factory;
    protected final RemoteRegistry<KEY, CONFIG_M, CONFIG_MB, ?> remoteRegistry;

    public RegistrySynchronizer(final Registry<KEY, ENTRY, ?> registry, final RemoteRegistry<KEY, CONFIG_M, CONFIG_MB, ?> remoteRegistry, final Factory<ENTRY, CONFIG_M> factory) throws org.dc.jul.exception.InstantiationException {
        try {
            this.localRegistry = registry;
            this.remoteRegistry = remoteRegistry;
            this.entryConfigDiff = new ProtobufListDiff<>();
            this.factory = factory;
            this.recurrenceSyncFilter = new RecurrenceEventFilter(15000) {

                @Override
                public void relay() throws Exception {
                    internalSync();
                }
            };

            this.remoteChangeObserver = (Observable<Map<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>>> source, Map<KEY, IdentifiableMessage<KEY, CONFIG_M, CONFIG_MB>> data) -> {
                sync();
            };

        } catch (Exception ex) {
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    public void init() throws CouldNotPerformException, InterruptedException {
        this.remoteRegistry.addObserver(remoteChangeObserver);
        try {
            this.internalSync();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Initial sync failed!", ex), logger, LogLevel.ERROR);
            try {
                if (JPService.getProperty(JPTestMode.class).getValue()) {
                    throw ex;
                }
            } catch (JPServiceException exx) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", exx), logger);
            }
        }
    }

    public void shutdown() {
        this.remoteRegistry.removeObserver(remoteChangeObserver);
        this.recurrenceSyncFilter.cancel();
    }

    private void sync() {
        recurrenceSyncFilter.trigger();
    }

    private synchronized void internalSync() throws CouldNotPerformException, InterruptedException {
        logger.info("Perform registry sync...");

        try {
            entryConfigDiff.diff(remoteRegistry.getMessages());

            MultiException.ExceptionStack removeExceptionStack = null;
            for (CONFIG_M config : entryConfigDiff.getRemovedMessageMap().getMessages()) {
                try {
                    remove(config);
                } catch (CouldNotPerformException ex) {
                    removeExceptionStack = MultiException.push(this, ex, removeExceptionStack);
                }
            }

            MultiException.ExceptionStack updateExceptionStack = null;
            for (CONFIG_M config : entryConfigDiff.getUpdatedMessageMap().getMessages()) {
                try {
                    if (verifyConfig(config)) {
                        update(config);
                    } else {
                        remove(config);
                        entryConfigDiff.getOriginMessages().removeMessage(config);
                    }
                } catch (CouldNotPerformException ex) {
                    updateExceptionStack = MultiException.push(this, ex, updateExceptionStack);
                }
            }

            MultiException.ExceptionStack registerExceptionStack = null;
            for (CONFIG_M config : entryConfigDiff.getNewMessageMap().getMessages()) {
                try {
                    if (verifyConfig(config)) {
                        register(config);
                    }
                } catch (CouldNotPerformException ex) {
                    registerExceptionStack = MultiException.push(this, ex, registerExceptionStack);
                }
            }

            int errorCounter = MultiException.size(removeExceptionStack) + MultiException.size(updateExceptionStack) + MultiException.size(registerExceptionStack);
            logger.info(entryConfigDiff.getChangeCounter() + " registry changes applied. " + errorCounter + " are skipped.");

            // sync origin list.
            IdentifiableMessageMap<KEY, CONFIG_M, CONFIG_MB> newOriginEntryMap = new IdentifiableMessageMap<>();
            for (ENTRY entry : localRegistry.getEntries()) {
                newOriginEntryMap.put(remoteRegistry.get(entry.getId()));
            }
            entryConfigDiff.replaceOriginMap(newOriginEntryMap);

            // build exception cause chain.
            MultiException.ExceptionStack exceptionStack = null;
            int counter;
            try {
                if(removeExceptionStack != null) {
                    counter = removeExceptionStack.size();
                } else {
                    counter = 0;
                }
                MultiException.checkAndThrow("Could not remove "+counter+" entries!", removeExceptionStack);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
            try {
                if(updateExceptionStack != null) {
                    counter = updateExceptionStack.size();
                } else {
                    counter = 0;
                }
                MultiException.checkAndThrow("Could not update "+counter+" entries!", updateExceptionStack);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
            try {
                if(registerExceptionStack != null) {
                    counter = registerExceptionStack.size();
                } else {
                    counter = 0;
                }
                MultiException.checkAndThrow("Could not register "+counter+" entries!", registerExceptionStack);
            } catch (CouldNotPerformException ex) {
                exceptionStack = MultiException.push(this, ex, exceptionStack);
            }
            MultiException.checkAndThrow("Could not sync all entries!", exceptionStack);

        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Entry registry sync failed!", ex);
        }
    }

    public ENTRY register(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return localRegistry.register(factory.newInstance(config));
    }

    public ENTRY update(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        ENTRY entry = localRegistry.get(remoteRegistry.getKey(config));
        entry.updateConfig(config);
        return entry;
    }

    public ENTRY remove(final CONFIG_M config) throws CouldNotPerformException, InterruptedException {
        return localRegistry.remove(getKey(config));
    }
    
    // TODO: move to interface or a util class. Redundant method - also used in RemoteRegistry and IdentifiableMessage
    public KEY getKey(final CONFIG_M entry) throws CouldNotPerformException {
        KEY key = (KEY) entry.getField(entry.getDescriptorForType().findFieldByName(TYPE_FIELD_ID));
        if (!localRegistry.contains(key)) {
            throw new CouldNotPerformException("Entry for given Key[" + key + "] is not available for local registry!");
        }
        return key;
    }

    /**
     * Method should return true if the given configurations is valid, otherwise false. This default implementation accepts all configurations. To implement a custom verification just overwrite this
     * method.
     *
     * @param config
     * @return
     * @throws org.dc.jul.exception.VerificationFailedException
     */
    public boolean verifyConfig(final CONFIG_M config) throws VerificationFailedException {
        return true;
    }
}
