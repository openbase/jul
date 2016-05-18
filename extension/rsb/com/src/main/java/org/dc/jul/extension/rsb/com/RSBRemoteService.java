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
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinTask;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
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
import static org.dc.jul.pattern.Remote.RemoteConnectionState.*;
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
public abstract class RSBRemoteService<M extends GeneratedMessage> implements RSBRemote<M> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected static int id = 0;
    protected int localId;

    public static final long REQUEST_TIMEOUT = 15000;
    public static final long PING_TIMEOUT = 3000;
    public static final long CONNECTION_TIMEOUT = 60000;
    public static final long DATA_WAIT_TIMEOUT = 1000;

    static {
        RSBSharedConnectionConfig.load();
    }

    private RSBListenerInterface listener;
    private WatchDog listenerWatchDog, remoteServerWatchDog;
    private RSBRemoteServerInterface remoteServer;
    private Remote.RemoteConnectionState connectionState;
    private long connectionPing;
    private long lastPingReceived;
    private final Handler mainHandler;

    private final SyncObject syncMonitor = new SyncObject("SyncMonitor");
    private final SyncObject connectionMonitor = new SyncObject("ConnectionMonitor");

    private CompletableFuture<M> syncFuture;
    private ForkJoinTask<M> syncTask;

    protected Scope scope;
    private M data;
    private boolean initialized;
    private final Class<M> messageClass;
    private final ObservableImpl<M> dataObservable = new ObservableImpl<>();

    public RSBRemoteService() {
        this.mainHandler = new InternalUpdateHandler();
        this.initialized = false;
        this.remoteServer = new NotInitializedRSBRemoteServer();
        this.listener = new NotInitializedRSBListener();
        this.messageClass = detectDataClass();
        localId = id;
        id++;
        this.connectionState = DISCONNECTED;
        this.connectionPing = -1;
        this.lastPingReceived = -1;
    }

    @Override
    public void init(final ScopeType.Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    @Override
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

    @Override
    public void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
        try {
            init(ScopeTransformer.transform(scope), participantConfig);
        } catch (CouldNotTransformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     * Method is called after communication initialization. You can overwrite
     * this method to trigger any component specific initialization.
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

    @Override
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

                    logger.debug("listener state update: " + data.name());
                    // Sync data after service start.
                    if (data == WatchDog.ServiceState.Running) {
                        remoteServerWatchDog.waitForActivation();
                        requestData();
                    }
                }
            });
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not create RemoteServer on scope [" + scope + "]!", ex);
        }
    }

    @Override
    public Class<M> getDataClass() {
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
            setConnectionState(CONNECTING);
            activateRemoteServer();
            activateListener();
        } catch (CouldNotPerformException ex) {
            throw new InvalidStateException("Could not activate remote service!", ex);
        }
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
//        try {
        try {
            validateInitialization();
        } catch (InvalidStateException ex) {
            // was never initialized!
            return;
        }
        setConnectionState(DISCONNECTED);
        skipSyncTasks();
        deactivateListener();
        deactivateRemoteServer();
//        } catch (CouldNotPerformException ex) {
//            throw new CouldNotPerformException("Could not deactivate " + getClass().getSimpleName() + "!", ex);
//        }
    }

    private void activateListener() throws InterruptedException {
        listenerWatchDog.activate();
    }

    private void deactivateListener() throws InterruptedException {
        listenerWatchDog.deactivate();
    }

    @Override
    public boolean isConnected() {
        return connectionState == CONNECTED;
    }

    private void setConnectionState(final RemoteConnectionState connectionState) {

        // filter unchanged events
        if (this.connectionState.equals(connectionState)) {
            return;
        }

        if (connectionState.equals(CONNECTED)) {
            ping();
        }

        System.out.println("connectionstate[" + localId + "]: " + connectionState.name());
        synchronized (connectionMonitor) {
            this.connectionState = connectionState;
            this.connectionMonitor.notifyAll();
        }
    }

    @Override
    public RemoteConnectionState getConnectionState() {
        return connectionState;
    }

    @Override
    public boolean isActive() {
        return listenerWatchDog.isActive() && remoteServerWatchDog.isActive();
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

    public final static long START_TIMEOUT = 500;
    public final static double TIMEOUT_MULTIPLIER = 1.2;
    public final static long MAX_TIMEOUT = 30000;

    @Override
    public <R, T extends Object> R callMethod(String methodName, T argument) throws CouldNotPerformException, InterruptedException {

        validateActivation();
        try {
            logger.info("Calling method [" + methodName + "(" + argument + ")] on scope: " + remoteServer.getScope().toString());
            if (!isConnected()) {
                waitForConnectionState(CONNECTED);
            }

            long timeout = START_TIMEOUT;
            while (true) {

                if (!isActive()) {
                    throw new InvalidStateException("Remote service is not active!");
                }

                try {
                    logger.info("Calling method [" + methodName + "(" + argument + ")] on scope: " + remoteServer.getScope().toString());
                    return remoteServer.call(methodName, argument, timeout);
                } catch (TimeoutException ex) {
                    ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                    timeout = generateTimeout(timeout);
                    logger.warn("Waiting for RPCServer[" + remoteServer.getScope() + "] to call method [" + methodName + "(" + argument + ")]. Next timeout in " + ((int) timeout) + " seconds.");
                    Thread.yield();
                }
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + argument + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }
    public final static Random jitterRandom = new Random();

    public static long generateTimeout(long currentTimeout) {
        return Math.min(MAX_TIMEOUT, (long) (currentTimeout * TIMEOUT_MULTIPLIER + jitterRandom.nextDouble()));
    }

    @Override
    public <R, T extends Object> Future<R> callMethodAsync(final String methodName, final T argument) throws CouldNotPerformException {

        validateActivation();
        return ForkJoinPool.commonPool().submit(new Callable<R>() {

            public Future<R> internalCallFuture;

            @Override
            public R call() throws Exception {
                try {
                    logger.debug("Calling method [" + methodName + "(" + (argument != null ? argument.toString() : "") + ")] on scope: " + remoteServer.getScope().toString());

                    if (!isConnected()) {
                        waitForConnectionState(CONNECTED);
                    }

                    internalCallFuture = remoteServer.callAsync(methodName, argument);
                    try {
                        return internalCallFuture.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
                    } catch (java.util.concurrent.TimeoutException ex) {
                        // validate connection
                        try {
                            ping().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
                        } catch (ExecutionException | java.util.concurrent.TimeoutException exx) {
                            // connection broken
                            internalCallFuture.cancel(true);
                        }
                        return internalCallFuture.get();
                    }
                } catch (InterruptedException ex) {
                    if (internalCallFuture != null) {
                        internalCallFuture.cancel(true);
                    }
                    throw ex;
                } catch (CouldNotPerformException ex) {
                    throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + argument + ")] on Scope[" + remoteServer.getScope() + "].", ex);
                }
            }
        });
    }

    /**
     * This method synchronizes this remote instance with the main controller
     * and returns the new data object. Normally, all server data changes are
     * automatically synchronized to all remote instances. In case you have
     * triggered many server changes, this changes are sequentially applied.
     * With this method you can force the sync to get instantly a data object
     * with all applied changes. This action can not be canceled! Use this
     * method with caution because high frequently calls will reduce the network
     * performance! The preferred by to access the data object
     *
     * @return A CompletableFuture which gives feedback about the successful
     * synchronization.
     * @throws CouldNotPerformException In case the sync could not be triggered
     * an CouldNotPerformException will be thrown.
     */
    @Override

    public CompletableFuture<M> requestData() throws CouldNotPerformException {
        System.out.println("RSBRemoteService[" + localId + "] requestData...");
        logger.info("requestData...");
        validateInitialization();
        try {
            synchronized (syncMonitor) {

                // Check if sync is in process.
                if (syncFuture != null) {
                    System.out.println("RSBRemoteService[" + localId + "] sync in progress");
                    return syncFuture;
                }

                // Create new sync process
                syncFuture = new CompletableFuture();
                System.out.println("RSBRemoteService[" + localId + "] syncing ...");
                syncTask = sync();
                return syncFuture;
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not request data!", ex);
        }
    }

    /**
     * Method forces a server - remote data sync and returns the new acquired
     * data. Can be useful for initial data sync or data sync after
     * reconnection.
     *
     * @return fresh synchronized data object.
     * @throws CouldNotPerformException
     */
    private ForkJoinTask<M> sync() throws CouldNotPerformException {
        System.out.println("Synchronization of Remote[" + localId + "][" + this + "] triggered...");
        logger.info("Synchronization of Remote[" + this + "] triggered...");
        validateInitialization();
        try {
            SyncTaskCallable syncCallable = new SyncTaskCallable();
            final ForkJoinTask<M> currentSyncTask = ForkJoinPool.commonPool().submit(syncCallable);
            syncCallable.setRelatedFuture(currentSyncTask);
            return currentSyncTask;
        } catch (java.util.concurrent.RejectedExecutionException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not request the current status.", ex);
        }
    }

    private class SyncTaskCallable implements Callable<M> {

        ForkJoinTask<M> relatedFuture;

        public void setRelatedFuture(ForkJoinTask<M> relatedFuture) {
            this.relatedFuture = relatedFuture;
        }

        @Override
        public M call() throws Exception {

            Future<Event> internalFuture = null;
            M dataUpdate;
            try {
                System.out.println("RSBRemoteServiceSyncCallable[" + localId + "] call request");
                logger.info("call request");
//                dataUpdate = callMethod(RPC_REQUEST_STATUS, messageClass);

                long timeout = START_TIMEOUT;
                while (true) {

                    if (!isActive()) {
                        throw new InvalidStateException("Remote service is not active!");
                    }

                    try {
                        internalFuture = remoteServer.callAsync(RPC_REQUEST_STATUS);
                        dataUpdate = (M) internalFuture.get(timeout, TimeUnit.MILLISECONDS).getData();
                        break;
                    } catch (TimeoutException ex) {
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                        timeout = generateTimeout(timeout);
                        logger.warn("Remote Controller["+ScopeTransformer.transform(getScope())+"] does not respond!  Next timeout in " + ((int) timeout) + " seconds.");
                        Thread.yield();
                    }
                }

                System.out.println("RSBRemoteServiceSyncCallable[" + localId + "] got data!");
                logger.info("got data!");

                if (dataUpdate == null) {
                    throw new InvalidStateException("Server result invalid!");
                }

                // skip if sync was already performed by global data update.
                if (relatedFuture == null || !relatedFuture.isCompletedNormally()) {
                    System.out.println("RSBRemoteServiceSyncCallable[" + localId + "] apply data update...");
                    applyDataUpdate(dataUpdate);
                } else {
                    System.out.println("RSBRemoteServiceSyncCallable[" + localId + "] skip because already synced!");
                }
                setConnectionState(CONNECTED);
                return dataUpdate;
            } catch (InterruptedException ex) {
                if (internalFuture != null) {
                    internalFuture.cancel(true);
                }
                throw ex;
            } catch (CouldNotPerformException ex) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Sync aborted!", ex), logger);
            }
        }
    }

    /**
     * This method deactivates the remote and cleans all resources.
     */
    @Override
    public void shutdown() {
        try {
            dataObservable.shutdown();
        } finally {
            try {
                deactivate();
            } catch (CouldNotPerformException | InterruptedException ex) {
                logger.error("Could not deactivate remote service!", ex);
            }
        }
    }

    /**
     * Returns a future of the data object. This method can be useful after
     * remote initialization in case the data object was not received jet. The
     * future can be used to wait for the data object.
     *
     * @return a future object delivering the data if available.
     * @throws CouldNotPerformException In case something went wrong a
     * CouldNotPerformException is thrown.
     */
    @Override
    public CompletableFuture<M> getDataFuture() throws CouldNotPerformException {
//        System.out.println("RSBRemoteService[" + localId + "] getDataFuture...");
        try {
            if (data == null) {
//                System.out.println("RSBRemoteService[" + localId + "] data is null. Calling request data.");
                return requestData();
            }
//            System.out.println("RSBRemoteService[" + localId + "] data is not null. Returning completed future with data.");
            return CompletableFuture.completedFuture(data);
        } catch (CouldNotPerformException ex) {
            throw new NotAvailableException("data", ex);
        }
    }

    /**
     * Method returns the data object of this remote which is synchronized with
     * the server data in background.
     *
     * In case the data was never received not available a NotAvailableException
     * is thrown. Use method getDataFuture()
     *
     * @return
     * @throws CouldNotPerformException
     */
    @Override
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
    @Override
    public boolean isDataAvailable() {
        return data != null;
    }

    @Override
    public void waitForData() throws CouldNotPerformException, InterruptedException {
//        System.out.println("RSBRemoteService[" + localId + "] waiting for data...");
        try {
//            System.out.println("RSBRemoteService[" + localId + "] calling get on getDataFuture...");
            getDataFuture().get();
//            System.out.println("RSBRemoteService[" + localId + "] calling get on getDataFuture finished");
        } catch (ExecutionException ex) {
            throw new CouldNotPerformException("Could not wait for data!", ex);
        }
//        System.out.println("RSBRemoteService[" + localId + "] waiting for data finished");
    }

    @Override
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException {
        try {
            getDataFuture().get(timeout, timeUnit);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            // TODO mpohling: should the interrupted exception thrown instead?
            throw new CouldNotPerformException("Interrupted while waiting for data!", ex);
        } catch (java.util.concurrent.TimeoutException | ExecutionException ex) {
            throw new NotAvailableException("Data is not yet available!", ex);
        }
    }

    private Class<M> detectDataClass() {
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

    private void validateInitialization() throws InvalidStateException {
        if (!initialized) {
            throw new InvalidStateException("Remote communication service not initialized!");
        }
    }

    private void validateActivation() throws InvalidStateException {
        if (!isActive()) {
            throw new InvalidStateException("Remote communication service not activated!");
        }
    }

    public void waitForConnectionState(final RemoteConnectionState connectionState) throws InterruptedException {
        synchronized (connectionMonitor) {
            while (!Thread.currentThread().isInterrupted()) {
                if (this.connectionState.equals(connectionState)) {
                    return;
                }
                System.out.println("wait_for_connection" + localId);
                connectionMonitor.wait();
                System.out.println("continue" + localId);
            }
        }
    }

    @Override
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
        System.out.println("RSBRemoteService[" + localId + "] apply data update....");
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
        try {
            System.out.println("RSBRemoteService[" + localId + "] notify data update...");
            notifyDataUpdate(data);
            System.out.println("RSBRemoteService[" + localId + "] notify data update finished");
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update!", ex), logger);
        }

        if (currentSyncFuture != null) {
            currentSyncFuture.complete(data);
        }

        if (currentSyncTask != null) {
            currentSyncTask.complete(data);
            setConnectionState(CONNECTED);
        }

        System.out.println("RSBRemoteService[" + localId + "] completed both sync futures");
        try {
//            System.out.println("RSBRemoteService[" + localId + "] notify observer...");
            dataObservable.notifyObservers(data);
//            System.out.println("RSBRemoteService[" + localId + "] notify observer finished");
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update to all observer!", ex), logger);
        }
        System.out.println("RSBRemoteService[" + localId + "] apply data update finished");
    }

    /**
     * Method can be overwritten to get internally informed about data updates.
     *
     * @param data new arrived data messages.
     * @throws CouldNotPerformException
     */
    protected void notifyDataUpdate(M data) throws CouldNotPerformException {
        // dummy method, please overwrite if needed.
    }

    /**
     *
     * @param observer
     * @deprecated use addDataObserver(observer); instead!
     */
    @Deprecated
    public void addObserver(Observer<M> observer) {
        addDataObserver(observer);
    }

    /**
     *
     * @param observer
     * @deprecated use removeDataObserver(observer); instead
     */
    @Deprecated
    public void removeObserver(Observer<M> observer) {
        removeDataObserver(observer);
    }

    @Override
    public void addDataObserver(final Observer<M> observer) {
        dataObservable.addObserver(observer);
    }

    @Override
    public void removeDataObserver(final Observer<M> observer) {
        dataObservable.removeObserver(observer);
    }

    public Future<Long> ping() {
//        System.out.println("compute ping");
        return ForkJoinPool.commonPool().submit(new Callable<Long>() {

            @Override
            public Long call() throws Exception {
                try {
                    Long requestTime = (Long) callMethodAsync("ping", System.currentTimeMillis()).get(PING_TIMEOUT, TimeUnit.MILLISECONDS);
                    lastPingReceived = System.currentTimeMillis();
                    connectionPing = lastPingReceived - requestTime;
//                    System.out.println("ping is: " + connectionPing);
                    return connectionPing;
                } catch (java.util.concurrent.TimeoutException ex) {
                    synchronized (connectionMonitor) {
                        if (connectionState == CONNECTED) {
                            logger.warn("Connection to Participant[" + ScopeTransformer.transform(getScope()) + "] lost!");
                            setConnectionState(CONNECTING);
                        }
                    }
                    throw ex;
                } catch (CouldNotPerformException | ExecutionException ex) {
                    throw new CouldNotPerformException("Could not compute ping!", ex);
                }
            }
        });
    }

    public long getPing() {
        return connectionPing;
    }

    private void skipSyncTasks() {
        logger.info("RSBRemoteService[" + localId + "] skip sync...");
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

        // Notify sync cancelation
        if (currentSyncFuture != null) {
            currentSyncFuture.cancel(true);
        }

        if (currentSyncTask != null) {
            currentSyncTask.cancel(true);
        }
    }
}
