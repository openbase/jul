/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBListenerInterface;
import de.citec.jul.extension.rsb.iface.RSBRemoteServerInterface;
import com.google.protobuf.Descriptors;
import de.citec.jul.extension.rsb.scope.ScopeProvider;
import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InitializationException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.TimeoutException;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import static de.citec.jul.extension.rsb.com.RSBCommunicationService.RPC_REQUEST_STATUS;
import de.citec.jul.extension.rsb.scope.ScopeTransformer;
import de.citec.jul.iface.Activatable;
import de.citec.jul.schedule.WatchDog;
import java.lang.reflect.ParameterizedType;
import java.util.Random;
import java.util.concurrent.Future;
import rsb.Event;
import rsb.Handler;
import rsb.Scope;
import rst.rsb.ScopeType;

/**
 *
 * @author mpohling
 * @param <M>
 */
public abstract class RSBRemoteService<M extends GeneratedMessage> extends Observable<M> implements Activatable {

    static {
        RSBSharedConnectionConfig.load();
    }
    
    private RSBListenerInterface listener;
    private WatchDog listenerWatchDog, remoteServerWatchDog;
    private final Handler mainHandler;
    private RSBRemoteServerInterface remoteServer;

    protected Scope scope;
    private M data;
    private boolean initialized;

    public RSBRemoteService() {
        this.mainHandler = new InternalUpdateHandler();
        this.initialized = false;
        this.remoteServer = new NotInitializedRSBRemoteServer();
        this.listener = new NotInitializedRSBListener();
    }

    public void init(final String label, final ScopeProvider location) throws InitializationException {
        try {
            init(generateScope(label, detectMessageClass(), location));
        } catch (CouldNotPerformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public synchronized void init(final ScopeType.Scope scope) throws InitializationException {
        try {
            init(ScopeTransformer.transform(scope));
        } catch (CouldNotTransformException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public synchronized void init(final Scope scope) throws InitializationException {

        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            if (initialized) {
                logger.warn("Skip initialization because " + this + " already initialized!");
                return;
            }

            this.scope = new Scope(scope.toString().toLowerCase());
            logger.debug("Init RSBCommunicationService for component " + getClass().getSimpleName() + " on " + this.scope + ".");

            initListener(this.scope);
            initRemoteServer(this.scope);

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

    private void initListener(final Scope scope) throws CouldNotPerformException {
        try {
            this.listener = RSBFactory.getInstance().createSynchronizedListener(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS));
            this.listenerWatchDog = new WatchDog(listener, "RSBListener[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS) + "]");
        } catch (InstantiationException ex) {
            throw new CouldNotPerformException("Could not create Listener on scope [" + scope + "]!", ex);
        }
    }

    private void initRemoteServer(final Scope scope) throws CouldNotPerformException {
        try {
            this.remoteServer = RSBFactory.getInstance().createSynchronizedRemoteServer(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL));
            this.remoteServerWatchDog = new WatchDog(remoteServer, "RSBRemoteServer[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL) + "]");
            this.listenerWatchDog.addObserver(new Observer<WatchDog.ServiceState>() {

                @Override
                public void update(Observable<WatchDog.ServiceState> source, WatchDog.ServiceState data) throws Exception {
                    if (data == WatchDog.ServiceState.Running) {

                        // Sync data after service start.
                        new Thread() {
                            @Override
                            public void run() {
                                try {
                                    remoteServerWatchDog.waitForActivation();
                                    sync();
                                } catch (InterruptedException | CouldNotPerformException ex) {
                                    ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not trigger data sync!", ex));
                                }
                            }
                        }.start();
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
        if (!initialized) {
            throw new InvalidStateException("Skip activation because " + this + " is not initialized!");
        }
        activateListener();
        activateRemoteServer();
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        try {
            if (!initialized) {
                throw new InvalidStateException("Skip deactivation because " + this + " is not initialized!");
            }
            deactivateListener();
            deactivateRemoteServer();
        } catch (InvalidStateException | InterruptedException ex) {
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
        return isActive();
    }

    @Override
    public boolean isActive() {
        return listenerWatchDog.isActive() && listener.isActive() && remoteServerWatchDog.isActive() && remoteServer.isActive();
    }

    private void activateRemoteServer() throws InterruptedException {
        remoteServerWatchDog.activate();
    }

    private void deactivateRemoteServer() throws InterruptedException {
        remoteServerWatchDog.deactivate();
    }

    public Object callMethod(String methodName) throws CouldNotPerformException {
        return callMethod(methodName, null);
    }

    public Future<Object> callMethodAsync(String methodName) throws CouldNotPerformException {
        return callMethodAsync(methodName, null);
    }

    public final static double START_TIMEOUT = 1;
    public final static double TIMEOUT_MULTIPLIER = 2;
    public final static double MAX_TIMEOUT = 30;

    public <R, T extends Object> R callMethod(String methodName, T type) throws CouldNotPerformException {

        if (!initialized) {
            throw new CouldNotPerformException("Skip invocation of Method[" + methodName + "] because " + this + " is not initialized!");
        }

        try {
            logger.info("Calling method [" + methodName + "(" + type + ")] on scope: " + remoteServer.getScope().toString());

            double timeout = START_TIMEOUT;
            while (true) {
                try {
                    return remoteServer.call(methodName, type, timeout);
                } catch (TimeoutException ex) {
                    timeout = generateTimeout(timeout);
                    logger.warn("Waiting for RPCServer[" + remoteServer.getScope() + "] to call method [" + methodName + "(" + type + ")]. Next timeout in " + ((int) timeout) + " seconds.");
                    Thread.yield();
                }
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }
    public final static Random jitterRandom = new Random();

    public static double generateTimeout(double currentTimeout) {
        return Math.min(MAX_TIMEOUT, currentTimeout * TIMEOUT_MULTIPLIER + jitterRandom.nextDouble());
    }

    public <R, T extends Object> Future<R> callMethodAsync(String methodName, T type) throws CouldNotPerformException {

        if (!initialized) {
            throw new CouldNotPerformException("Skip invocation of Method[" + methodName + "] because " + this + " is not initialized!");
        }
        try {
            logger.info("Calling method [" + methodName + "(" + type + ")] on scope: " + remoteServer.getScope().toString());
            return remoteServer.callAsync(methodName, type);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Methode[" + methodName + "(" + type + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }

    protected void sync() throws CouldNotPerformException {
        Future<Object> initialSyncFuture = callMethodAsync(RPC_REQUEST_STATUS);
    }

    /**
     * Triggers a server - remote data sync and returns the new acquired data.
     * All server data changes are synchronized automatically to all remote instances.
     * In case you have triggered many server changes, you can use this method to get instantly a data object with all applied changes.
     *
     * Note: This method blocks until the new data is acquired!
     *
     * @return fresh synchronized data object.
     * @throws CouldNotPerformException
     */
    public M requestStatus() throws CouldNotPerformException {
        try {
            logger.debug("requestStatus updated.");
            M dataUpdate = (M) callMethod(RPC_REQUEST_STATUS);
            
            if(dataUpdate == null) {
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
        super.shutdown();
    }

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

    public static Scope generateScope(final String label, final Class typeClass, final ScopeProvider scopeProvider) throws CouldNotPerformException {
        try {
            return scopeProvider.getScope().concat(new Scope(Scope.COMPONENT_SEPARATOR + typeClass.getSimpleName())).concat(new Scope(Scope.COMPONENT_SEPARATOR + label));

        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Coult not generate scope!", ex);
        }
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[scope:" + scope + "]";
    }

    public abstract void notifyUpdated(M data) throws CouldNotPerformException;

    private class InternalUpdateHandler implements Handler {

        @Override
        public void internalNotify(Event event) {
            logger.debug("Internal notification: " + event.toString());
            try {
                applyDataUpdate((M) event.getData());
            } catch (Exception ex) {
                ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not unpack data update! Received Datatype[" + event.getData().getClass().getName() + "] is not compatible with " + getClass().getName() + "]!", ex));
            }
        }
    }

    private synchronized void applyDataUpdate(final M data) {
        logger.info("Data update for " + this);
        this.data = data;

        try {
            notifyUpdated(data);
        } catch (Exception ex) {
            ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not notify data update!", ex));
        }
        try {
            notifyObservers(data);
        } catch (Exception ex) {
            ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not notify data update to all observer!", ex));
        }
    }
}

// TODO mpohling: Config for test setup.
//        ParticipantConfig config = rsb.Factory.getInstance().getDefaultParticipantConfig();
//        
//        for (Map.Entry<String,TransportConfig> transport : config.getTransports().entrySet()) {
//            transport.getValue().setEnabled(false);
//        }
//        config.getTransports().get("inmemory").setEnabled(true);
//        Informer informer = rsb.Factory.getInstance().createInformer(new Scope("/"), config);
