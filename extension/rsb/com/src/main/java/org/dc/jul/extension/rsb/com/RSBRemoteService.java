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
import java.util.Random;
import java.util.concurrent.Callable;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;
import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.CouldNotTransformException;
import org.dc.jul.exception.InitializationException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.InvalidStateException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.TimeoutException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import static org.dc.jul.extension.rsb.com.RSBCommunicationService.RPC_REQUEST_STATUS;
import org.dc.jul.extension.rsb.com.jp.JPRSBTransport;
import org.dc.jul.extension.rsb.iface.RSBListenerInterface;
import org.dc.jul.extension.rsb.iface.RSBRemoteServerInterface;
import org.dc.jul.extension.rsb.scope.ScopeGenerator;
import org.dc.jul.extension.rsb.scope.ScopeTransformer;
import org.dc.jul.pattern.Observable;
import org.dc.jul.pattern.ObservableImpl;
import org.dc.jul.pattern.Observer;
import org.dc.jul.pattern.Remote;
import org.dc.jul.schedule.SyncObject;
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
public abstract class RSBRemoteService<M extends GeneratedMessage> extends ObservableImpl<M> implements Remote<M> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final long REQUEST_TIMEOUT = 15000;

    static {
        RSBSharedConnectionConfig.load();
    }

    private RSBListenerInterface listener;
    private WatchDog listenerWatchDog, remoteServerWatchDog;
    private RSBRemoteServerInterface remoteServer;
    private final Handler mainHandler;

//    private final List<CompletableFuture<M>> syncTasks;
    private final SyncObject syncMonitor = new SyncObject(this);
    private CompletableFuture<M> syncFuture;
    private ForkJoinTask<M> syncTask;

    protected Scope scope;
    private M data;
    private boolean initialized;
    private final Class<M> messageClass;

    public RSBRemoteService() {
        this.mainHandler = new InternalUpdateHandler();
        this.initialized = false;
        this.remoteServer = new NotInitializedRSBRemoteServer();
        this.listener = new NotInitializedRSBListener();
        this.messageClass = detectMessageClass();
    }

    public void init(final ScopeType.Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    public void init(final Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    @Override
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

    /**
     * Method is called after communication initialization.
     * You can overwrite this method to trigger any component specific initialization.
     *
     * @throws InitializationException
     * @throws InterruptedException
     */
    protected void postInit() throws InitializationException, InterruptedException {
        // overwrite for specific post initialization tasks.
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

            postInit();
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
                public void update(final Observable<WatchDog.ServiceState> source, WatchDog.ServiceState data) throws Exception {
                    if (data == WatchDog.ServiceState.Running) {

                        // Sync data after service start.
//                        Future<Rep> result = new Future<>();
//    Future.runAsync(() -> {
//
//        transporter.write(req);
//        try {
//            Rep rep = responseQueue.take();
//            result.complete(rep);
//        } catch (InterruptedException e) {
//            result.completeExceptionally(e);
//            Thread.currentThread().interrupt();
//        } catch (Exception e) {
//            result.completeExceptionally(e);
//        }
//
//    }, executorService);
//    return result;
                        syncTasks.add(Future.runAsync(() -> {
                            try {
                                Rep rep = responseQueue.take();
                                result.complete(rep);
                            } catch (InterruptedException e) {
                                result.completeExceptionally(e);
                                Thread.currentThread().interrupt();
                            } catch (Exception e) {
                                result.completeExceptionally(e);
                            }
                        }
                            new Supplier<Void>















                            () {

                            @Override
                            public Void get



                        () {
//                                throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
                            }
                        }));
                        syncTasks.add(Future.run(new Callable<Void>() {

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

    @Override
    public Class<M> getMessageClass() {
        return messageClass;
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
            skipSyncTasks();
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

    @Override
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

    //Timeout needed!
    @Override
    public Object callMethod(String methodName) throws CouldNotPerformException, InterruptedException {
        return callMethod(methodName, null);
    }

    @Override
    public Future<Object> callMethodAsync(String methodName) throws CouldNotPerformException {
        return callMethodAsync(methodName, null);
    }

    public final static double START_TIMEOUT = 5;
    public final static double TIMEOUT_MULTIPLIER = 1.2;
    public final static double MAX_TIMEOUT = 30;

    @Override
    public <R, T extends Object> R callMethod(String methodName, T type) throws CouldNotPerformException, InterruptedException {
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
//        } catch (InterruptedException ex) {
            //TODO mpohling: handle interrupted exception in paramite release
//            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }
    public final static Random jitterRandom = new Random();

    public static double generateTimeout(double currentTimeout) {
        return Math.min(MAX_TIMEOUT, currentTimeout * TIMEOUT_MULTIPLIER + jitterRandom.nextDouble());
    }

    @Override
    public <R, T extends Object> Future<R> callMethodAsync(String methodName, T type) throws CouldNotPerformException {
        try {
            logger.debug("Calling method [" + methodName + "(" + (type != null ? type.toString() : "") + ")] on scope: " + remoteServer.getScope().toString());
            validateConnectionState();
            return remoteServer.callAsync(methodName, type);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }

    /**
     * This method synchronizes this remote instance with the main controller and returns the new data object.
     * Normally, all server data changes are automatically synchronized to all remote instances. In case you have triggered many server
     * changes, this changes are sequentially applied. With this method you can force the sync to get instantly a data object with all applied changes.
     * This action can not be canceled! Use this method with caution because high frequently calls will reduce the network performance!
     * The preferred by to access the data object
     *
     * @return A CompletableFuture which gives feedback about the successful synchronization.
     * @throws CouldNotPerformException In case the sync could not be triggered an CouldNotPerformException will be thrown.
     */
    @Override
    public CompletableFuture<M> requestData() throws CouldNotPerformException {
        try {
            synchronized (syncMonitor) {

                // Check if sync is in process.
                if (syncFuture != null) {
                    return syncFuture;
                }

                // Create new sync process
                syncFuture = new CompletableFuture();
                syncTask = sync();
                return syncFuture;
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not request data!", ex);
        }
    }

    /**
     * Method forces a server - remote data sync and returns the new acquired data.
     * Can be useful for initial data sync or data sync after reconnection.
     *
     * @return fresh synchronized data object.
     * @throws CouldNotPerformException
     */
    protected ForkJoinTask<M> sync() throws CouldNotPerformException {
        logger.info("Synchronization of Remote[" + this + "] triggered...");

        try {
            SyncTaskCallable<M> syncCallable = new SyncTaskCallable<>();
            final ForkJoinTask<M> currentSyncTask = ForkJoinPool.commonPool().submit(syncCallable);
            syncCallable.setRelatedFuture(currentSyncTask);
            return currentSyncTask;
        } catch (java.util.concurrent.RejectedExecutionException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not request the current status.", ex);
        }
    }

    private class SyncTaskCallable<M> implements Callable<M> {

        ForkJoinTask<M> relatedFuture;

        public void setRelatedFuture(ForkJoinTask<M> relatedFuture) {
            this.relatedFuture = relatedFuture;
        }

        @Override
        public M call() throws Exception {
            Future<M> internalFuture = null;
            M dataUpdate;
            try {
                internalFuture = callMethodAsync(RPC_REQUEST_STATUS, messageClass);
                dataUpdate = internalFuture.get();

                if (dataUpdate == null) {
                    throw new InvalidStateException("Server result invalid!");
                }

                // skip if sync was already performed by global data update.
                if (relatedFuture == null || !relatedFuture.isCompletedNormally()) {
                    applyDataUpdate(dataUpdate);
                }
                return dataUpdate;
            } catch (InterruptedException ex) {
                if (internalFuture != null) {
                    internalFuture.cancel(true);
                }
                throw ex;
            }
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
        super.shutdown();
    }

    /**
     * Returns a future of the data object.
     * This method can be useful after remote initialization in case the data object was not received jet.
     * The future can be used to wait for the data object.
     *
     * @return a future object delivering the data if available.
     * @throws CouldNotPerformException In case something went wrong a CouldNotPerformException is thrown.
     */
    public CompletableFuture<M> getDataFuture() throws CouldNotPerformException {
        try {
            if (data == null) {
                return requestData();
            }
            return CompletableFuture.completedFuture(data);
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("data", ex);
        }
    }

    /**
     * Method returns the data object of this remote which is synchronized with the server data in background.
     *
     * In case the data was never received not available a NotAvailableException is thrown.
     * Use method getDataFuture()
     *
     * @return
     * @throws CouldNotPerformException
     */
    public M getData() throws CouldNotPerformException {
        if (data == null) {
            throw new NotAvailableException("data");
        }
        return data;
    }

    /**
     * Check if the data object is already available.
     *
     * @return
     */
    public boolean isDataAvailable() {
        return data != null;
    }



    private final Class<M> detectMessageClass() {
        ParameterizedType parameterizedType = (ParameterizedType) getClass().getGenericSuperclass();
        return (Class<M>) parameterizedType.getActualTypeArguments()[0];
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
        //TODO mpohling: remove after connection handshake is implemented.
        if (!isActive()) {
            throw new InvalidStateException("Could not reach server! Remote is not activated!");
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
        } catch (CouldNotTransformException ex) {
            throw new NotAvailableException("scope", ex);
        }
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[scope:" + scope + "]";
    }

    private class InternalUpdateHandler implements Handler {

        @Override
        public void internalNotify(Event event) {
            try {
                logger.debug("Internal notification: " + event.toString());
                Object data = event.getData();
                try {
                    applyDataUpdate((M) data);
                } catch (ClassCastException ex) {
                    // Thats not the right internal data type. Skip update...
                }
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Internal notification failed!", ex), logger);
            }
        }
    }

    private void applyDataUpdate(final M data) {
        this.data = data;
        CompletableFuture<M> currentSyncFuture = null;
        ForkJoinTask<M> currentSyncTask = null;

        // Check if sync is in process.
        synchronized (syncMonitor) {
            if (syncFuture != null) {
                currentSyncFuture = syncFuture;
                currentSyncTask = syncTask;
                syncFuture = null;
                syncTask = null;
            }
        }

        // Notify data update
        if (currentSyncFuture != null) {
            currentSyncFuture.complete(data);
        }

        if (currentSyncTask != null) {
            currentSyncTask.complete(data);
        }

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
}
