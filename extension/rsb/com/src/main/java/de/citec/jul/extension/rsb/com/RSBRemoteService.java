/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import de.citec.jps.core.JPService;
import de.citec.jps.exception.JPServiceException;
import de.citec.jps.preset.JPTestMode;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.exception.InitializationException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.exception.TimeoutException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.printer.LogLevel;
import static de.citec.jul.extension.rsb.com.RSBCommunicationService.RPC_REQUEST_STATUS;
import de.citec.jul.extension.rsb.iface.RSBListenerInterface;
import de.citec.jul.extension.rsb.iface.RSBRemoteServerInterface;
import de.citec.jul.extension.rsb.scope.ScopeProvider;
import de.citec.jul.extension.rsb.scope.ScopeTransformer;
import de.citec.jul.iface.Activatable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import de.citec.jul.schedule.WatchDog;
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
public abstract class RSBRemoteService<M extends GeneratedMessage> extends Observable<M> implements Activatable {

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

    public void init(final Scope scope) throws InitializationException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    public synchronized void init(final String scope) throws InitializationException {
        try {
            init(new Scope(scope));
        } catch (Exception ex) {
            throw new InitializationException(this, ex);
        }
    }

    public synchronized void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException {
        try {

            ParticipantConfig internalParticipantConfig = participantConfig;
            try {
                // activate inprocess communication for junit tests.
                if (JPService.getProperty(JPTestMode.class).getValue()) {
                    for (Map.Entry<String, TransportConfig> transport : internalParticipantConfig.getTransports().entrySet()) {
                        transport.getValue().setEnabled(false);
                    }
                    internalParticipantConfig.getOrCreateTransport("inprocess").setEnabled(true);
                }
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

            this.scope = new Scope(scope.toString().toLowerCase());
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
            checkInitialization();
            activateListener();
            activateRemoteServer();
        } catch (CouldNotPerformException ex) {
            throw new InvalidStateException("Could not activate remote service!", ex);
        }
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        try {
            checkInitialization();

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
            checkInitialization();
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
            checkInitialization();

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
            checkInitialization();
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
                        throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Data sync failed!", ex), logger, LogLevel.DEBUG);
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

    private void checkInitialization() throws InvalidStateException {
        if (!initialized) {
            throw new InvalidStateException("Remote communication service not initialized!");
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
            applyDataUpdate((M) event.getData());
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
}
