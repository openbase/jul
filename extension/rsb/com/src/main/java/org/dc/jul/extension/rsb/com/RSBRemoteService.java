/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
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
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import java.lang.reflect.ParameterizedType;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPTestMode;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.CouldNotTransformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.exception.TimeoutException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import static org.dc.jul.extension.rsb.com.RSBCommunicationService.RPC_REQUEST_STATUS;
import org.dc.jul.extension.rsb.com.jp.JPRSBTransport;
import org.dc.jul.extension.rsb.iface.RSBListenerInterface;
import org.dc.jul.extension.rsb.iface.RSBRemoteServerInterface;
import org.dc.jul.extension.rsb.scope.ScopeGenerator;
import org.dc.jul.extension.rsb.scope.ScopeTransformer;
import org.dc.jul.extension.rst.iface.ScopeProvider;
import org.dc.jul.iface.Activatable;
import org.dc.jul.iface.Shutdownable;
import org.dc.jul.pattern.Observable;
import org.dc.jul.pattern.Observer;
import org.dc.jul.schedule.WatchDog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Handler;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;
import rst.rsb.ScopeType;

/**
 *
 * @author mpohling
 * @param <M>
 */
public abstract class RSBRemoteService<M extends GeneratedMessage> extends Observable<M> implements Shutdownable, Activatable {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    static {
        RSBSharedConnectionConfig.load();
    }

    private RSBListenerInterface listener;
    private WatchDog listenerWatchDog, remoteServerWatchDog;
    private RSBRemoteServerInterface remoteServer;
    private final Handler mainHandler;
    private final ExecutorService executorService;
    private final List<Future<Void>> syncTasks;

    protected Scope scope;
    private M data;
    private boolean initialized;

    public RSBRemoteService() {
        this.mainHandler = new InternalUpdateHandler();
        this.initialized = false;
        this.remoteServer = new NotInitializedRSBRemoteServer();
        this.listener = new NotInitializedRSBListener();
        this.executorService = Executors.newCachedThreadPool();
        this.syncTasks = new ArrayList<>();
    }

    public void init(final ScopeType.Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    public void init(final Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    public void init(final String scope) throws InitializationException, InterruptedException {
        try {
            init(new Scope(scope));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
        try {
            init(ScopeTransformer.transform(scope), participantConfig);
        } catch (CouldNotTransformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    private void enableTransport(final ParticipantConfig participantConfig, final JPRSBTransport.TransportType type) {
        if (type == JPRSBTransport.TransportType.DEFAULT) {
            return;
        }

        for (TransportConfig transport : participantConfig.getEnabledTransports()) {
            logger.debug("Disable " + transport.getName() + " communication.");
            transport.setEnabled(false);
        }
        logger.debug("Enable [" + type.name().toLowerCase() + "] communication.");
        participantConfig.getOrCreateTransport(type.name().toLowerCase()).setEnabled(true);
    }

    public synchronized void init(final ScopeType.Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
        try {
            ParticipantConfig internalParticipantConfig = participantConfig;
            try {
                // activate transport communication set by the JPRSBTransport porperty.
                enableTransport(internalParticipantConfig, JPService.getProperty(JPRSBTransport.class).getValue());
            } catch (JPServiceException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            }

            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            if (initialized) {
                logger.warn("Skip initialization because " + this + " already initialized!");
                return;
            }

            this.scope = new Scope(ScopeGenerator.generateStringRep(scope).toLowerCase());
            logger.debug("Init RSBCommunicationService for component " + getClass().getSimpleName() + " on " + this.scope + ".");

            initListener(this.scope, internalParticipantConfig);
            initRemoteServer(this.scope, internalParticipantConfig);

            try {
                addHandler(mainHandler, true);
            } catch (InterruptedException ex) {
                logger.warn("Could not register main handler!", ex);
            }
            initialized = true;
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    private void initListener(final Scope scope, final ParticipantConfig participantConfig) throws CouldNotPerformException {
        try {
            this.listener = RSBFactory.getInstance().createSynchronizedListener(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS), participantConfig);
            this.listenerWatchDog = new WatchDog(listener, "RSBListener[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS) + "]");
        } catch (InstantiationException ex) {
            throw new CouldNotPerformException("Could not create Listener on scope [" + scope + "]!", ex);
        }
    }

    private void initRemoteServer(final Scope scope, final ParticipantConfig participantConfig) throws CouldNotPerformException {
        try {
            this.remoteServer = RSBFactory.getInstance().createSynchronizedRemoteServer(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL), participantConfig);
            this.remoteServerWatchDog = new WatchDog(remoteServer, "RSBRemoteServer[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL) + "]");
            this.listenerWatchDog.addObserver(new Observer<WatchDog.ServiceState>() {


                @Override
                public void update(Observable<WatchDog.ServiceState> source, WatchDog.ServiceState data) throws Exception {
                    if (data == WatchDog.ServiceState.Running) {

                        // Sync data after service start.
                        syncTasks.add(executorService.submit(new Callable<Void>() {

                            @Override
                            public Void call() throws Exception {
                                try {
                                    remoteServerWatchDog.waitForActivation();
                                    sync();
                                } catch (InterruptedException | CouldNotPerformException ex) {
                                    throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Could not trigger data sync!", ex), logger, LogLevel.ERROR);
                                }
                                return null;
                            }
                        }));
                    }
                }
            });
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not create RemoteServer on scope [" + scope + "]!", ex);
        }
    }

    public void addHandler(final Handler handler, final boolean wait) throws InterruptedException, CouldNotPerformException {
        try {
            listener.addHandler(handler, wait);
        } catch (InterruptedException ex) {
            throw new CouldNotPerformException("Could not register Handler!", ex);
        }
    }

    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        try {
            validateInitialization();
            activateListener();
            activateRemoteServer();
        } catch (CouldNotPerformException ex) {
            throw new InvalidStateException("Could not activate remote service!", ex);
        }
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        try {
            validateInitialization();

            // skip sync tasks
            syncTasks.forEach((Future<Void> task) -> task.cancel(true));

            deactivateListener();
            deactivateRemoteServer();
        } catch (InvalidStateException ex) {
            throw new CouldNotPerformException("Could not deactivate " + getClass().getSimpleName() + "!", ex);
        }
    }

    private void activateListener() throws InterruptedException {
        listenerWatchDog.activate();
    }

    private void deactivateListener() throws InterruptedException {
        listenerWatchDog.deactivate();
    }

    public boolean isConnected() {
        //TODO mpohling implement connection server check.

        if (!isActive()) {
            return false;
        }

        if (data == null) {
            try {
                sync().get(500, TimeUnit.SECONDS);
            } catch (Exception ex) {
                // ignore if sync failed.
            }
        }

        return data != null;
    }

    @Override
    public boolean isActive() {
        try {
            validateInitialization();
        } catch (InvalidStateException ex) {
            return false;
        }
        return listenerWatchDog.isActive() && listener.isActive() && remoteServerWatchDog.isActive() && remoteServer.isActive();
    }

    private void activateRemoteServer() throws InterruptedException {
        remoteServerWatchDog.activate();
    }

    private void deactivateRemoteServer() throws InterruptedException {
        remoteServerWatchDog.deactivate();
    }

    public Object callMethod(String methodName) throws CouldNotPerformException, InterruptedException {
        return callMethod(methodName, null);
    }

    public Future<Object> callMethodAsync(String methodName) throws CouldNotPerformException {
        return callMethodAsync(methodName, null);
    }

    public final static double START_TIMEOUT = 5;
    public final static double TIMEOUT_MULTIPLIER = 1.2;
    public final static double MAX_TIMEOUT = 30;

    public <R, T extends Object> R callMethod(String methodName, T type) throws CouldNotPerformException {
        try {
            logger.debug("Calling method [" + methodName + "(" + type + ")] on scope: " + remoteServer.getScope().toString());
            validateConnectionState();

            double timeout = START_TIMEOUT;
            while (true) {

                if (!isActive()) {
                    throw new InvalidStateException("Remote service is not active!");
                }

                try {
                    return remoteServer.call(methodName, type, timeout);
                } catch (TimeoutException ex) {
                    ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                    timeout = generateTimeout(timeout);
                    logger.warn("Waiting for RPCServer[" + remoteServer.getScope() + "] to call method [" + methodName + "(" + type + ")]. Next timeout in " + ((int) timeout) + " seconds.");
                    Thread.yield();
                }
            }
        } catch (InterruptedException ex) {
            //TODO mpohling: handle interrupted exception in paramite release
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }
    public final static Random jitterRandom = new Random();

    public static double generateTimeout(double currentTimeout) {
        return Math.min(MAX_TIMEOUT, currentTimeout * TIMEOUT_MULTIPLIER + jitterRandom.nextDouble());
    }

    public <R, T extends Object> Future<R> callMethodAsync(String methodName, T type) throws CouldNotPerformException {
        try {
            logger.debug("Calling method [" + methodName + "(" + (type != null ? type.toString() : "") + ")] on scope: " + remoteServer.getScope().toString());
            validateConnectionState();
            return remoteServer.callAsync(methodName, type);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }

    protected Future<Object> sync() throws CouldNotPerformException {
        final Future<Object> dataSyncFuture = callMethodAsync(RPC_REQUEST_STATUS);

        //TODO mpohling: switch to Future<M> return value by defining message class via construtor.
        // sumbit task for result processing
        executorService.submit(new Callable<Void>() {

            @Override
            public Void call() throws Exception {
                try {
                    applyDataUpdate((M) dataSyncFuture.get(1, TimeUnit.MINUTES));
                } catch (Exception ex) {
                    dataSyncFuture.cancel(true);
                    if (ex instanceof InterruptedException) {
                        // BUG! InterruptedException is catched!!! // BUG BUG BUB BUG
                        throw ex;
//                        throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Data sync failed!", ex), logger, LogLevel.DEBUG);
                    } else {
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Data sync failed!", ex), logger, LogLevel.ERROR);
                    }
                }
                return null;
            }
        });
        return dataSyncFuture;
    }

    /**
     * Triggers a server - remote data sync and returns the new acquired data. All server data changes are synchronized automatically to all remote instances. In case you have triggered many server
     * changes, you can use this method to get instantly a data object with all applied changes.
     *
     * Note: This method blocks until the new data is acquired!
     *
     * @return fresh synchronized data object.
     * @throws CouldNotPerformException
     */
    public M requestStatus() throws CouldNotPerformException {
        try {
            logger.debug("requestStatus updated.");
            M dataUpdate;
            try {
                dataUpdate = (M) callMethod(RPC_REQUEST_STATUS);
            } catch (InterruptedException ex) {
                // TODO mpohling: forward interupted exception in paramite release.
                throw new RejectedException("Remote call was interrupted!", ex);
//                throw ex;
            }

            if (dataUpdate == null) {
                throw new InvalidStateException("Server result invalid!");
            }

            applyDataUpdate(dataUpdate);
            return dataUpdate;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not request the current status.", ex);
        }
    }

    /**
     * This method deactivates the remote and cleans all resources.
     */
    @Override
    public void shutdown() {
        try {
            deactivate();
        } catch (CouldNotPerformException | InterruptedException ex) {
            logger.error("Could not deactivate remote service!", ex);
        }
        executorService.shutdownNow();
        super.shutdown();
    }

    /**
     * Returns the data object of the given remote.
     *
     * @return
     * @throws CouldNotPerformException
     */
    public M getData() throws CouldNotPerformException {
        try {
            if (data == null) {
                return requestStatus();
            }
            return data;
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("data", ex);
        }
    }

    public Class detectMessageClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class) parameterizedType.getActualTypeArguments()[0];
    }

    protected final Object getField(String name) throws CouldNotPerformException {
        try {
            Descriptors.FieldDescriptor findFieldByName = getData().getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + name + "] does not exist for type " + getData().getClass().getName());
            }
            return getData().getField(findFieldByName);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not return value of field [" + name + "] for " + this, ex);
        }
    }

    private void validateConnectionState() throws InvalidStateException {
        validateInitialization();
        if (!isConnected()) {
            throw new InvalidStateException("Could not reach server!");
        }
    }

    private void validateInitialization() throws InvalidStateException {
        if (!initialized) {
            throw new InvalidStateException("Remote communication service not initialized!");
        }
    }
    public ScopeType.Scope getScope() throws NotAvailableException {
        try {
            return ScopeTransformer.transform(scope);
        } catch(CouldNotTransformException ex) {
            throw new NotAvailableException("scope", ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[scope:" + scope + "]";
    }

    public abstract void notifyUpdated(M data) throws CouldNotPerformException;

    private class InternalUpdateHandler implements Handler {

        @Override
        public void internalNotify(Event event) {
            try {
                logger.debug("Internal notification: " + event.toString());
                Object data = event.getData();
                try {
                    applyDataUpdate((M) data);
                    return;
                } catch (ClassCastException ex) {
                    // Thats not the right internal data type. Skip update...
                }

                try {
                    incomingEvent(event);
                } catch (RuntimeException ex) {
                    throw ex;
                } catch (Exception ex) {
                    throw new CouldNotPerformException("Error during incoming event notification of RemoteService["+this+"]", ex);
                }

            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Internal notification failed!", ex), logger);
            }
        }
    }

    private synchronized void applyDataUpdate(final M data) {
        this.data = data;

        try {
            notifyUpdated(data);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update!", ex), logger, LogLevel.ERROR);
        }
        try {
            notifyObservers(data);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update to all observer!", ex), logger, LogLevel.ERROR);
        }
    }

    /**
     * Overwrite this method to get informed about incoming events.
     *
     * @param event
     */
    protected void incomingEvent(Event event) {
        // Methode dummy, overwirte to get informed about incoming event.
    }
}
