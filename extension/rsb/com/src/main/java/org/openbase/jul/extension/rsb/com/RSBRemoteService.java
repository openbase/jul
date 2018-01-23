package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
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

import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.TimeoutException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.processing.MessageProcessor;
import org.openbase.jul.extension.protobuf.processing.SimpleMessageProcessor;
import org.openbase.jul.extension.rsb.iface.RSBListener;
import org.openbase.jul.extension.rsb.iface.RSBRemoteServer;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.openbase.jul.extension.rsb.scope.ScopeTransformer;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.Remote;
import org.openbase.jul.schedule.GlobalCachedExecutorService;
import org.openbase.jul.schedule.SyncObject;
import org.openbase.jul.schedule.WatchDog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Handler;
import rsb.config.ParticipantConfig;
import rst.rsb.ScopeType.Scope;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.*;

import static org.openbase.jul.extension.rsb.com.RSBCommunicationService.RPC_REQUEST_STATUS;
import static org.openbase.jul.pattern.Remote.ConnectionState.*;

/**
 * @param <M>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class RSBRemoteService<M extends GeneratedMessage> implements RSBRemote<M> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public static final long REQUEST_TIMEOUT = 15000;
    /**
     * Timeout in seconds since this goes to a synchronous call where it is given in seconds :/.
     */
    public static final long PING_TIMEOUT = 5;
    public static final long PING_TEST_TIMEOUT = 1;
    public static final long CONNECTION_TIMEOUT = 30000;
    public static final long DATA_WAIT_TIMEOUT = 1000;
    public static final long LOGGING_TIMEOUT = 15000;
    public static final long METHOD_CALL_START_TIMEOUT = 500;
    public static final double METHOD_CALL_TIMEOUT_MULTIPLIER = 1.2;
    public static final long METHOD_CALL_MAX_TIMEOUT = 30000;

    private static final Random JITTER_RANDOM = new Random();

    private RSBListener listener;
    private WatchDog listenerWatchDog, remoteServerWatchDog;
    private RSBRemoteServer remoteServer;
    private Remote.ConnectionState connectionState;
    private long connectionPing;
    private long lastPingReceived;
    private final Handler mainHandler;

    private final SyncObject syncMonitor = new SyncObject("SyncMonitor");
    private final SyncObject connectionMonitor = new SyncObject("ConnectionMonitor");
    private final SyncObject maintainerLock = new SyncObject("MaintainerLock");
    private final SyncObject pingLock = new SyncObject("PingLock");
    protected Object maintainer;

    private CompletableFuture<M> syncFuture;
    private Future<M> syncTask;

    protected Scope scope;
    private M data;
    private boolean initialized;
    private final Class<M> dataClass;
    private MessageProcessor<GeneratedMessage, M> messageProcessor;
    private Set<StackTraceElement[]> reinitStackTraces = new HashSet<>();

    private final ObservableImpl<ConnectionState> connectionStateObservable = new ObservableImpl<>(this);
    private final ObservableImpl<M> internalPriorizedDataObservable = new ObservableImpl<>(this);
    private final ObservableImpl<M> dataObservable = new ObservableImpl<>(this);

    private boolean shutdownInitiated;

    private long newestEventTime = 0;
    private long newestEventTimeNano = 0;

    private boolean connectionFailure = false;
    private Future<Long> pingTask = null;

    public RSBRemoteService(final Class<M> dataClass) {
        this.dataClass = dataClass;
        this.mainHandler = new InternalUpdateHandler();
        this.initialized = false;
        this.shutdownInitiated = false;
        this.remoteServer = new NotInitializedRSBRemoteServer();
        this.listener = new NotInitializedRSBListener();
        this.connectionState = DISCONNECTED;
        this.connectionPing = -1;
        this.lastPingReceived = -1;
        this.messageProcessor = new SimpleMessageProcessor<>(dataClass);
        this.connectionStateObservable.setExecutorService(GlobalCachedExecutorService.getInstance().getExecutorService());
    }

    public void setMessageProcessor(MessageProcessor<GeneratedMessage, M> messageProcessor) {
        this.messageProcessor = messageProcessor;
    }

    /**
     * {@inheritDoc}
     *
     * @param scope {@inheritDoc}
     * @throws org.openbase.jul.exception.InitializationException {@inheritDoc}
     * @throws java.lang.InterruptedException                     {@inheritDoc}
     */
    @Override
    public void init(final Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    /**
     * {@inheritDoc}
     *
     * @param scope {@inheritDoc}
     * @throws org.openbase.jul.exception.InitializationException {@inheritDoc}
     * @throws java.lang.InterruptedException                     {@inheritDoc}
     */
    @Override
    public void init(final rsb.Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    /**
     * Initialize the remote on a scope.
     *
     * @param scope the scope where the remote communicates
     * @throws InitializationException if the initialization fails
     * @throws InterruptedException    if the initialization is interrupted
     */
    @Override
    public void init(final String scope) throws InitializationException, InterruptedException {
        try {
            init(new rsb.Scope(scope));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param scope             {@inheritDoc}
     * @param participantConfig {@inheritDoc}
     * @throws InitializationException {@inheritDoc}
     * @throws InterruptedException    {@inheritDoc}
     */
    @Override
    public void init(final rsb.Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
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

    /**
     * {@inheritDoc}
     *
     * @param scope             {@inheritDoc}
     * @param participantConfig {@inheritDoc}
     * @throws org.openbase.jul.exception.InitializationException {@inheritDoc}
     * @throws java.lang.InterruptedException                     {@inheritDoc}
     */
    @Override
    public void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
        internalInit(scope, participantConfig);
    }

    private void internalInit(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
        synchronized (maintainerLock) {
            try {
                verifyMaintainability();

                // only set to reconnecting when already active, this way init can be used when not active
                // to update the config and when active the sync task still does not get cancelled
                if (isActive()) {
                    setConnectionState(ConnectionState.RECONNECTING);
                } else {
                    setConnectionState(ConnectionState.REINITIALIZING);
                }

                ParticipantConfig internalParticipantConfig = participantConfig;

                if (scope == null) {
                    throw new NotAvailableException("scope");
                }

                // check if this instance was partly or fully initialized before.
                if (initialized | listenerWatchDog != null | remoteServerWatchDog != null) {
                    deactivate();
                    reset();
                }

                this.scope = scope;

                rsb.Scope internalScope = new rsb.Scope(ScopeGenerator.generateStringRep(scope).toLowerCase());
                logger.debug("Init RSBCommunicationService for component " + getClass().getSimpleName() + " on " + this.scope + ".");

                initListener(internalScope, internalParticipantConfig);
                initRemoteServer(internalScope, internalParticipantConfig);

                addHandler(mainHandler, true);

                postInit();
                initialized = true;


                // check if remote service was already activated before and recover state.
                switch (getConnectionState()) {
                    case RECONNECTING:
                        activate();
                        break;
                    case REINITIALIZING:
                        setConnectionState(ConnectionState.DISCONNECTED);
                        break;
                }
            } catch (CouldNotPerformException ex) {
                throw new InitializationException(this, ex);
            }
        }
    }

    private void initListener(final rsb.Scope scope, final ParticipantConfig participantConfig) throws CouldNotPerformException {
        try {
            this.listener = RSBFactoryImpl.getInstance().createSynchronizedListener(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS), participantConfig);
            this.listenerWatchDog = new WatchDog(listener, "RSBListener[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_STATUS) + "]");
        } catch (InstantiationException ex) {
            throw new CouldNotPerformException("Could not create Listener on scope [" + scope + "]!", ex);
        }
    }

    private void initRemoteServer(final rsb.Scope scope, final ParticipantConfig participantConfig) throws CouldNotPerformException {
        try {
            this.remoteServer = RSBFactoryImpl.getInstance().createSynchronizedRemoteServer(scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL), participantConfig);
            this.remoteServerWatchDog = new WatchDog(remoteServer, "RSBRemoteServer[" + scope.concat(RSBCommunicationService.SCOPE_SUFFIX_CONTROL) + "]");
            this.listenerWatchDog.addObserver((final Observable<WatchDog.ServiceState> source, WatchDog.ServiceState data1) -> {
                logger.debug("listener state update: " + data1.name());
                // Sync data after service start.
                if (data1 == WatchDog.ServiceState.RUNNING) {
                    remoteServerWatchDog.waitForServiceActivation();
                    requestData();
                }
            });
        } catch (RuntimeException | InstantiationException ex) {
            throw new CouldNotPerformException("Could not create RemoteServer on scope [" + scope + "]!", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws VerificationFailedException {@inheritDoc}
     */
    @Override
    public void verifyMaintainability() throws VerificationFailedException {
        if (isLocked()) {
            throw new VerificationFailedException("Manipulation of " + this + "is currently not valid because the maintains is protected by another instance! "
                    + "Did you try to modify an instance which is locked by a managed instance pool?");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isLocked() {
        synchronized (maintainerLock) {
            return maintainer != null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void lock(final Object maintainer) throws CouldNotPerformException {
        synchronized (maintainerLock) {
            if (this.maintainer != null) {
                throw new CouldNotPerformException("Could not lock remote because it is already locked by another instance!");
            }
            this.maintainer = maintainer;
        }
    }

    /**
     * Method unlocks this instance.
     *
     * @param maintainer the instance which currently holds the lock.
     * @throws CouldNotPerformException is thrown if the instance could not be
     *                                  unlocked.
     */
    @Override
    public void unlock(final Object maintainer) throws CouldNotPerformException {
        synchronized (maintainerLock) {
            if (this.maintainer != null && this.maintainer != maintainer) {
                throw new CouldNotPerformException("Could not unlock remote because it is locked by another instance!");
            }
            this.maintainer = null;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Class<M> getDataClass() {
        return dataClass;
    }

    /**
     * Method adds an handler to the internal rsb listener.
     *
     * @param handler
     * @param wait
     * @throws InterruptedException
     * @throws CouldNotPerformException
     */
    public void addHandler(final Handler handler, final boolean wait) throws InterruptedException, CouldNotPerformException {
        try {
            listener.addHandler(handler, wait);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not register Handler!", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException     {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        synchronized (maintainerLock) {

            // Duplicated activation filter.
            if (isActive()) {
                return;
            }

            try {
                verifyMaintainability();
                validateInitialization();
                setConnectionState(CONNECTING);
                remoteServerWatchDog.activate();
                listenerWatchDog.activate();
            } catch (CouldNotPerformException ex) {
                throw new InvalidStateException("Could not activate remote service!", ex);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException     {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void activate(boolean waitForData) throws InterruptedException, CouldNotPerformException {
        activate();

        if (waitForData) {
            // make sure the remote is fully synchronized with main controller before continue.
            waitForData();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException     {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void activate(final Object maintainer) throws InterruptedException, CouldNotPerformException {
        if (!isLocked() || this.maintainer.equals(maintainer)) {
            synchronized (maintainerLock) {
                unlock(maintainer);
                activate();
                lock(maintainer);
            }
        } else {
            throw new VerificationFailedException("[" + maintainer + "] is not the current maintainer of this remote");
        }
    }

    /**
     * Atomic deactivate which makes sure that the maintainer stays the same.
     *
     * @param maintainer the current maintainer of this remote
     * @throws InterruptedException        if deactivation is interrupted
     * @throws CouldNotPerformException    if deactivation fails
     * @throws VerificationFailedException is thrown if the given maintainer does not match the current one
     */
    public void deactivate(final Object maintainer) throws InterruptedException, CouldNotPerformException, VerificationFailedException {
        if (this.maintainer.equals(maintainer)) {
            synchronized (maintainerLock) {
                unlock(maintainer);
                deactivate();
                lock(maintainer);
            }
        } else {
            throw new VerificationFailedException("[" + maintainer + "] is not the current maintainer of this remote");
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException     {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        synchronized (maintainerLock) {
            try {
                verifyMaintainability();
                validateInitialization();
            } catch (InvalidStateException ex) {
                // was never initialized!
                return;
            }
            if (connectionState != ConnectionState.RECONNECTING) {
                skipSyncTasks();
                setConnectionState(DISCONNECTED);
                if (pingTask != null && !pingTask.isDone()) {
                    pingTask.cancel(true);
                }
            }
            if (listenerWatchDog != null) {
                listenerWatchDog.deactivate();
            }

            if (remoteServerWatchDog != null) {
                remoteServerWatchDog.deactivate();
            }
        }
        synchronized (connectionMonitor) {
            connectionMonitor.notifyAll();
        }
    }

    public void reset() throws CouldNotPerformException {
        try {
            verifyMaintainability();

            // clear init flag
            initialized = false;

            // clear existing instances.
            if (listenerWatchDog != null) {
                listenerWatchDog.shutdown();
                listenerWatchDog = null;
                listener = new NotInitializedRSBListener();
            }
            if (remoteServerWatchDog != null) {
                remoteServerWatchDog.shutdown();
                remoteServerWatchDog = null;
                remoteServer = new NotInitializedRSBRemoteServer();
            }
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not reset " + this + "!", ex);
        }
    }

    /**
     * Method reinitialize this remote. If the remote was previously active the activation state will be recovered.
     * This method can be used in case of a broken connection or if the participant config has been changed.
     * <p>
     * Note: After reinit the data remains the same but a new sync task is created. So to make sure to have new data
     * it is necessary to call {@code requestData.get()}.
     *
     * @throws InterruptedException     is thrown if the current thread was externally interrupted.
     * @throws CouldNotPerformException is throws if the reinit has been failed.
     */
    protected void reinit() throws InterruptedException, CouldNotPerformException {
        reinit(scope);
    }

    /**
     * Method reinitialize this remote. If the remote was previously active the activation state will be recovered.
     * This method can be used in case of a broken connection or if the participant config has been changed.
     * <p>
     * Note: After reinit the data remains the same but a new sync task is created. So to make sure to have new data
     * it is necessary to call {@code requestData.get()}.
     *
     * @param scope the new scope to configure.
     * @throws InterruptedException     is thrown if the current thread was externally interrupted.
     * @throws CouldNotPerformException is throws if the reinit has been failed.
     */
    protected void reinit(final Scope scope) throws InterruptedException, CouldNotPerformException {

        // to not reinit if shutdown is in progress!
        if (shutdownInitiated) {
            return;
        }

        final StackTraceElement[] stackTraceElement = Thread.currentThread().getStackTrace();
        try {
            synchronized (maintainerLock) {
                reinitStackTraces.add(stackTraceElement);

                if (reinitStackTraces.size() > 1) {
                    for (final StackTraceElement[] trace : reinitStackTraces) {
                        StackTracePrinter.printStackTrace("Dublicated reinit call by:", trace, logger, LogLevel.WARN);
                    }
                    throw new FatalImplementationErrorException("dublicated reinit detected!", this);
                }
                logger.debug("Reinit " + this);
                // temporally unlock this remove but save maintainer
                final Object currentMaintainer = maintainer;
                try {
                    maintainer = null;

                    // reinit remote
                    internalInit(scope, RSBSharedConnectionConfig.getParticipantConfig());
                } catch (final CouldNotPerformException ex) {
                    throw new CouldNotPerformException("Could not reinit " + this + "!", ex);
                } finally {
                    // restore maintainer
                    maintainer = currentMaintainer;
                }
            }
        } catch (final CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not reinitialize " + this + "!", ex);
        } finally {
            reinitStackTraces.remove(stackTraceElement);
        }
    }

    /**
     * Method reinitialize this remote. If the remote was previously active the activation state will be recovered.
     * This method can be used in case of a broken connection or if the participant config has been changed.
     * <p>
     * Note: After reinit the data remains the same but a new sync task is created. So to make sure to have new data
     * it is necessary to call {@code requestData.get()}.
     *
     * @param maintainer the current maintainer of this remote
     * @throws InterruptedException        is thrown if the current thread was externally interrupted.
     * @throws CouldNotPerformException    is throws if the reinit has been failed.
     * @throws VerificationFailedException is thrown if the given maintainerLock does not match the current maintainer
     */
    public void reinit(final Object maintainer) throws InterruptedException, CouldNotPerformException, VerificationFailedException {
        reinit(scope, maintainer);
    }

    /**
     * Method reinitialize this remote. If the remote was previously active the activation state will be recovered.
     * This method can be used in case of a broken connection or if the participant config has been changed.
     * <p>
     * Note: After reinit the data remains the same but a new sync task is created. So to make sure to have new data
     * it is necessary to call {@code requestData.get()}.
     *
     * @param scope      the new scope to configure.
     * @param maintainer the current maintainer of this remote
     * @throws InterruptedException        is thrown if the current thread was externally interrupted.
     * @throws CouldNotPerformException    is throws if the reinit has been failed.
     * @throws VerificationFailedException is thrown if the given maintainerLock does not match the current maintainer
     */
    public void reinit(final Scope scope, final Object maintainer) throws InterruptedException, CouldNotPerformException, VerificationFailedException {
        if (this.maintainer.equals(maintainer)) {
            reinit(scope);
        } else {
            throw new VerificationFailedException("Manipulation of " + this + "is not valid using lock[" + maintainerLock + "]");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isConnected() {
        return connectionState == CONNECTED;
    }

    private void setConnectionState(final ConnectionState connectionState) {
        synchronized (connectionMonitor) {
            if (this.connectionState == ConnectionState.RECONNECTING && connectionState == CONNECTED) {
                // while reconnecting and not yet deactivated a data update can cause switching to connected which causes
                // a second re-init because the resulting ping fails, skip setting to connected to prevent the ping while
                // still applying the data update
                // re-init will switch to connecting anyway when activating again
                return;
            }

            // filter unchanged events
            if (this.connectionState.equals(connectionState)) {
                return;
            }

            // update state and notify
            final ConnectionState oldConnectionState = this.connectionState;
            this.connectionState = connectionState;

            // handle state related actions
            switch (connectionState) {
                case DISCONNECTED:
                    break;
                case CONNECTING:
                    // if disconnected before the data request is already initiated.
                    if (isActive() && oldConnectionState != DISCONNECTED) {
                        connectionFailure = true;
                        try {
                            requestData();
                        } catch (CouldNotPerformException ex) {
                            ExceptionPrinter.printHistory(new CouldNotPerformException("Reconnection failed!", ex), logger, LogLevel.WARN);
                        }
                    }
                    break;
                case CONNECTED:
                    if (connectionFailure) {
                        logger.info("Connection reestablished " + this);
                    } else {
                        logger.debug("Connection established " + this);
                    }
                    connectionFailure = false;

                    // make sure to start a new ping to detect the correct connection quality
                    // this has to be done in a seperate task since ping can cause a change of the
                    // connection state and this part is inside the connectionMonitor
                    GlobalCachedExecutorService.submit(() -> {
                        synchronized (pingLock) {
                            if (pingTask != null && !pingTask.isDone()) {
                                try {
                                    pingTask.get();
                                } catch (ExecutionException | CancellationException ex) {
                                    // exception handling is already done by the ping task itself.
                                    return;
                                } catch (InterruptedException ex) {
                                    Thread.currentThread().interrupt();
                                    return;
                                }
                            }
                            ping();
                        }
                    });
                    break;
            }

            this.connectionMonitor.notifyAll();
            try {
                this.connectionStateObservable.notifyObservers(connectionState);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify ConnectionState[" + connectionState + "] change to all observers!", ex), logger);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public ConnectionState getConnectionState() {
        return connectionState;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        try {
            return listenerWatchDog.isActive() && remoteServerWatchDog.isActive();
        } catch (NullPointerException ex) {
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     */
    @Override
    public <R> Future<R> callMethodAsync(final String methodName) throws CouldNotPerformException {
        return callMethodAsync(methodName, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     * @throws java.lang.InterruptedException                      {@inheritDoc}
     */
    @Override
    public <R> R callMethod(final String methodName) throws CouldNotPerformException, InterruptedException {
        return callMethod(methodName, null);
    }

    /**
     * {@inheritDoc}
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     * @throws java.lang.InterruptedException                      {@inheritDoc}
     */
    @Override
    public <R, T extends Object> R callMethod(final String methodName, final T argument) throws CouldNotPerformException, InterruptedException {
        return callMethod(methodName, argument, -1);
    }

    /**
     * {@inheritDoc}
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     * @throws java.lang.InterruptedException                      {@inheritDoc}
     */
    @Override
    public <R> R callMethod(String methodName, long timeout) throws CouldNotPerformException, TimeoutException, InterruptedException {
        return callMethod(methodName, null, timeout);
    }

    /**
     * {@inheritDoc}
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     * @throws java.lang.InterruptedException                      {@inheritDoc}
     */
    @Override
    public <R, T extends Object> R callMethod(final String methodName, final T argument, final long timeout) throws CouldNotPerformException, TimeoutException, InterruptedException {

        final String shortArgument = RPCHelper.argumentToString(argument);
        validateMiddleware();
        long retryTimeout = METHOD_CALL_START_TIMEOUT;
        long validTimeout = timeout;

        try {
            logger.debug("Calling method [" + methodName + "(" + shortArgument + ")] on scope: " + remoteServer.getScope().toString());
            if (!isConnected()) {
                waitForConnectionState(CONNECTED, timeout);
            }

            if (timeout > -1) {
                retryTimeout = Math.min(METHOD_CALL_START_TIMEOUT, validTimeout);
            }

            while (true) {

                if (!isActive()) {
                    throw new InvalidStateException("Remote service is not active!");
                }

                try {
                    logger.debug("Calling method [" + methodName + "(" + shortArgument + ")] on scope: " + remoteServer.getScope().toString());
                    remoteServerWatchDog.waitForServiceActivation(timeout, TimeUnit.MILLISECONDS);
                    final R returnValue = remoteServer.call(methodName, argument, retryTimeout);

                    if (retryTimeout != METHOD_CALL_START_TIMEOUT && retryTimeout > 15000) {
                        logger.info("Method[" + methodName + "(" + shortArgument + ")] returned! Continue processing...");
                    }
                    return returnValue;

                } catch (TimeoutException ex) {

                    // check if timeout is set and handle
                    if (timeout != -1) {
                        validTimeout -= retryTimeout;
                        if (validTimeout <= 0) {
                            ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                            throw new TimeoutException("Could not call remote Method[" + methodName + "(" + shortArgument + ")] on Scope[" + remoteServer.getScope() + "] in Time[" + timeout + "ms].");
                        }
                        retryTimeout = Math.min(generateTimeout(retryTimeout), validTimeout);
                    } else {
                        retryTimeout = generateTimeout(retryTimeout);
                    }

                    // only print warning if timeout is too long.
                    if (retryTimeout > 15000) {
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                        logger.warn("Waiting for RPCServer[" + remoteServer.getScope() + "] to call method [" + methodName + "(" + shortArgument + ")]. Next retry timeout in " + (int) (Math.floor(retryTimeout / 1000)) + " sec.");
                    } else {
                        ExceptionPrinter.printHistory(ex, logger, LogLevel.DEBUG);
                        logger.debug("Waiting for RPCServer[" + remoteServer.getScope() + "] to call method [" + methodName + "(" + shortArgument + ")]. Next retry timeout in " + (int) (Math.floor(retryTimeout / 1000)) + " sec.");
                    }

                    Thread.yield();
                }
            }
        } catch (TimeoutException ex) {
            throw ex;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not call remote Method[" + methodName + "(" + shortArgument + ")] on Scope[" + remoteServer.getScope() + "].", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param <R>        {@inheritDoc}
     * @param <T>        {@inheritDoc}
     * @param methodName {@inheritDoc}
     * @param argument   {@inheritDoc}
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public <R, T extends Object> Future<R> callMethodAsync(final String methodName, final T argument) throws CouldNotPerformException {

        validateMiddleware();
        return GlobalCachedExecutorService.submit(new Callable<R>() {

            private Future<R> internalCallFuture;

            @Override
            public R call() throws Exception {

                final String shortArgument = RPCHelper.argumentToString(argument);

                try {
                    try {
                        logger.debug("Calling method async [" + methodName + "(" + shortArgument + ")] on scope: " + remoteServer.getScope().toString());

                        if (!isConnected()) {
                            try {
                                waitForConnectionState(CONNECTED, CONNECTION_TIMEOUT);
                            } catch (TimeoutException ex) {
                                throw new CouldNotPerformException("Cannot not call async method[" + methodName + "(" + shortArgument + ")] on [" + this + "] in connectionState[" + connectionState + "]", ex);

                            }
                        }
                        remoteServerWatchDog.waitForServiceActivation();
                        internalCallFuture = remoteServer.callAsync(methodName, argument);
                        while (true) {
                            try {
                                return internalCallFuture.get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
                            } catch (java.util.concurrent.TimeoutException ex) {
                                // validate connection
                                try {
                                    // if reconnecting do not start a ping but just wait for connecting again
                                    if (getConnectionState() != ConnectionState.RECONNECTING) {
                                        ping().get(REQUEST_TIMEOUT, TimeUnit.MILLISECONDS);
                                    } else {
                                        waitForConnectionState(CONNECTING);
                                    }
                                } catch (ExecutionException | java.util.concurrent.TimeoutException | CancellationException exx) {
                                    // cancel call if connection is broken
                                    if (internalCallFuture != null) {
                                        internalCallFuture.cancel(true);
                                    }
                                }
                            } catch (InterruptedException ex) {
                                Thread.currentThread().interrupt();
                            }

                            // check if thread was interrupted during processing
                            if (Thread.currentThread().isInterrupted()) {
                                throw new InterruptedException();
                            }
                        }
                    } catch (final InterruptedException ex) {
                        if (internalCallFuture != null) {
                            internalCallFuture.cancel(true);
                        }
                        throw ex;
                    } catch (final InvalidStateException ex) {
                        // reinit remote service because middleware connection lost!
                        switch (connectionState) {
                            // only if the connection was established before and no reconnect is ongoing.
                            case CONNECTING:
                            case CONNECTED:
                                try {
                                    reinit();
                                } catch (final CouldNotPerformException exx) {
                                    ExceptionPrinter.printHistory("Recovering middleware connection failed!", exx, logger);
                                }
                        }
                        throw ex;
                    }
                } catch (final CouldNotPerformException | ExecutionException | CancellationException | InterruptedException ex) {
                    throw new CouldNotPerformException("Could not call remote Method[" + methodName + "(" + shortArgument + ")] on Scope[" + remoteServer.getScope() + "].", ex);
                }
            }
        });
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public CompletableFuture<M> requestData() throws CouldNotPerformException {
        logger.debug(this + " requestData...");
        validateInitialization();
        try {
            synchronized (syncMonitor) {

                // Check if sync is in process.
                if (syncFuture != null && !syncFuture.isDone()) {

                    // Recover sync task if those was canceled for instance during remote reinitialization.
                    if (syncTask == null || syncTask.isDone()) {
                        syncTask = sync();
                    }
                    return syncFuture;
                } else {
                    // cleanup old sync task
                    if (syncTask != null && !syncTask.isDone()) {
                        syncTask.cancel(true);
                        ExceptionPrinter.printHistory(new FatalImplementationErrorException("Old sync task was still running without known sync future!", this), logger);
                    }
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
     * Method forces a server - remote data sync and returns the new acquired
     * data. Can be useful for initial data sync or data sync after
     * reconnection.
     *
     * @return fresh synchronized data object.
     * @throws CouldNotPerformException
     */
    private Future<M> sync() throws CouldNotPerformException {
        logger.debug("Synchronization of Remote[" + this + "] triggered...");
        validateInitialization();
        try {
            SyncTaskCallable syncCallable = new SyncTaskCallable();

            final Future<M> currentSyncTask = GlobalCachedExecutorService.submit(syncCallable);
            syncCallable.setRelatedFuture(currentSyncTask);
            return currentSyncTask;
        } catch (java.util.concurrent.RejectedExecutionException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not request the current status.", ex);
        }
    }

    private class SyncTaskCallable implements Callable<M> {

        private Future<M> relatedFuture;

        public void setRelatedFuture(Future<M> relatedFuture) {
            this.relatedFuture = relatedFuture;
        }

        private boolean isRelatedFutureCancelled() {
            return relatedFuture != null && relatedFuture.isCancelled();
        }

        @Override
        public M call() throws CouldNotPerformException {

            Future<Event> internalFuture = null;
            M dataUpdate = null;
            Event event = null;
            boolean active = isActive();
            try {
                try {
                    logger.debug("call request");

                    long timeout = METHOD_CALL_START_TIMEOUT;
                    while (true) {
                        // if reconnecting wait until activated again
                        if (getConnectionState() == ConnectionState.RECONNECTING) {
                            waitForConnectionState(ConnectionState.CONNECTING);
                            waitForMiddleware();
                        }

                        synchronized (maintainerLock) {
                            // update activation state
                            active = isActive();

                            // needed for synchronization, it happened that between this check and checking
                            // again when catching the exception the remote has activated
                            if (!active) {
                                // if not active the sync is not possible and will be canceled to avoid executor service overload. After next remote activation a new sync is triggered anyway.
                                if (shutdownInitiated && syncFuture != null && !syncFuture.isDone()) {
                                    syncFuture.cancel(true);
                                }
                                throw new InvalidStateException("Remote service is not active within ConnectionState[\" + getConnectionState().name() + \"] and sync will be triggered after reactivation, so current sync is skipped.!");
                            }
                        }

                        waitForMiddleware();

                        // handle shutdown
                        if (shutdownInitiated) {
                            if (shutdownInitiated && syncFuture != null && !syncFuture.isDone()) {
                                syncFuture.cancel(true);
                            }
                            return null;
                        }

                        try {
                            try {
                                internalFuture = remoteServer.callAsync(RPC_REQUEST_STATUS);
                            } catch (CouldNotPerformException ex) {
                                logger.warn("Something went wrong during data request, maybe the connection or activation state has just changed so all checks will be performed again...", ex);
                                continue;
                            }
                            try {
                                event = internalFuture.get(timeout, TimeUnit.MILLISECONDS);
                            } catch (final java.util.concurrent.ExecutionException ex) {
                                // still apply the timeout to avoid fast error loops.
                                Thread.sleep(timeout);
                                throw ex;
                            }

                            dataUpdate = messageProcessor.process((GeneratedMessage) event.getData());
                            if (timeout != METHOD_CALL_START_TIMEOUT && timeout > 15000 && isRelatedFutureCancelled()) {
                                logger.info("Got response from Controller[" + ScopeTransformer.transform(getScope()) + "] and continue processing.");
                            }
                            break;

                        } catch (java.util.concurrent.TimeoutException ex) {

                            // handle interruption.
                            if (ex.getCause() instanceof InterruptedException) {
                                throw ex;
                            }

                            // cancel internal future because it will be recreated within the next iteration anyway.
                            if (internalFuture != null) {
                                internalFuture.cancel(true);
                            }

                            // if sync was already performed by global data update skip sync
                            if (isRelatedFutureCancelled()) {
                                return data;
                            }

                            timeout = generateTimeout(timeout);

                            // only print warning if timeout is too long.
                            if (timeout > 15000) {
                                ExceptionPrinter.printHistory(ex, logger, LogLevel.WARN);
                                logger.warn("Controller[" + ScopeTransformer.transform(getScope()) + "] does not respond!  Next retry timeout in " + (int) (Math.floor(timeout / 1000)) + " sec.");
                            } else {
                                ExceptionPrinter.printHistory(ex, logger, LogLevel.DEBUG);
                                logger.debug("Controller[" + ScopeTransformer.transform(getScope()) + "] does not respond!  Next retry timeout in " + (int) (Math.floor(timeout / 1000)) + " sec.");
                            }
                        }
                    }

                    if (dataUpdate == null) {
                        // do not set to connecting while reconnecting because when timed wrong this can cause
                        // reinit to cancel sync tasks and reinit while switch to connecting anyway when finished
                        if (getConnectionState() == ConnectionState.RECONNECTING) {
                            return data;
                        }
                        ExceptionPrinter.printVerboseMessage("Remote connection to Controller[" + ScopeTransformer.transform(getScope()) + "] was detached because the controller shutdown was initiated.", logger);
                        setConnectionState(CONNECTING);
                        return data;
                    }

                    // skip if sync was already performed by global data update.
                    if (relatedFuture == null || !relatedFuture.isCancelled()) {
                        if (event != null) {
                            // skip events which were send later than the last received update
                            if (event.getMetaData().getCreateTime() > newestEventTime || (event.getMetaData().getCreateTime() == newestEventTime && event.getMetaData().getUserTime(RPCHelper.USER_TIME_KEY) > newestEventTimeNano)) {
                                newestEventTime = event.getMetaData().getCreateTime();
                                if (event.getMetaData().hasUserTime(RPCHelper.USER_TIME_KEY)) {
                                    newestEventTimeNano = event.getMetaData().getUserTime(RPCHelper.USER_TIME_KEY);
                                }
                                applyDataUpdate(dataUpdate);
                            } else {
                                logger.debug("Skip event on scope[" + ScopeGenerator.generateStringRep(event.getScope()) + "] because creation time is lower than time of last event [" + event.getMetaData().getCreateTime() + ", " + newestEventTime + "][" + event.getMetaData().getUserTime(RPCHelper.USER_TIME_KEY) + ", " + newestEventTimeNano + "]");
                            }
                        }
                    }
                    return dataUpdate;
                } catch (InterruptedException ex) {
                    if (internalFuture != null) {
                        internalFuture.cancel(true);
                    }
                    return null;
                }
            } catch (CouldNotPerformException | CancellationException ex) {
                if (shutdownInitiated || !active || getConnectionState().equals(DISCONNECTED)) {
                    throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Sync aborted of " + getScopeStringRep(), ex), logger, LogLevel.DEBUG);
                } else {
                    throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Sync aborted of " + getScopeStringRep(), ex), logger);
                }
            } catch (Exception ex) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new FatalImplementationErrorException(this, ex), logger);
            }
        }
    }

    /**
     * This method deactivates the remote and cleans all resources.
     */
    @Override
    public void shutdown() {
        try {
            verifyMaintainability();
        } catch (VerificationFailedException ex) {
            throw new RuntimeException("Can not shutdown " + this + "!", ex);
        }

        this.shutdownInitiated = true;
        try {
            dataObservable.shutdown();
        } finally {
            try {
                deactivate();
            } catch (CouldNotPerformException | InterruptedException ex) {
                ExceptionPrinter.printHistory("Could not shutdown " + this + "!", ex, logger);
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public M getData() throws NotAvailableException {
        if (data == null) {
            throw new NotAvailableException("data");
        }
        return data;
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isDataAvailable() {
        return data != null;
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException     {@inheritDoc}
     */
    @Override
    public void waitForData() throws CouldNotPerformException, InterruptedException {
        try {
            if (isDataAvailable()) {
                return;
            }
            logger.debug("Wait for " + this.toString() + " data...");
            getDataFuture().get();
            dataObservable.waitForValue();
        } catch (ExecutionException | CancellationException ex) {
            throw new CouldNotPerformException("Could not wait for data!", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @param timeout  {@inheritDoc}
     * @param timeUnit {@inheritDoc}
     * @throws CouldNotPerformException       {@inheritDoc}
     * @throws java.lang.InterruptedException {@inheritDoc}
     */
    @Override
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        try {
            if (isDataAvailable()) {
                return;
            }
            long startTime = System.currentTimeMillis();
            getDataFuture().get(timeout, timeUnit);
            long partialTimeout = timeUnit.toMillis(timeout) - (System.currentTimeMillis() - startTime);
            if (partialTimeout <= 0) {
                throw new java.util.concurrent.TimeoutException("Data timeout is reached!");
            }
            dataObservable.waitForValue(partialTimeout, TimeUnit.MILLISECONDS);
        } catch (java.util.concurrent.TimeoutException | CouldNotPerformException | ExecutionException | CancellationException ex) {
            throw new NotAvailableException("Data is not yet available!", ex);
        }
    }

    protected final Object getDataField(String name) throws CouldNotPerformException {
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

    protected final boolean hasDataField(final String name) throws CouldNotPerformException {
        try {
            Descriptors.FieldDescriptor findFieldByName = getData().getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                return false;
            }
            return getData().hasField(findFieldByName);
        } catch (Exception ex) {
            return false;
        }
    }

    public void validateInitialization() throws InvalidStateException {
        if (!initialized) {
            throw new InvalidStateException(this + " not initialized!");
        }
    }

    public void validateActivation() throws InvalidStateException {
        validateInitialization();
        if (!isActive()) {
            throw new InvalidStateException(this + " not activated!");
        }
    }

    public void validateMiddleware() throws InvalidStateException {
        validateActivation();

        try {
            if (listener == null) {
                throw new InvalidStateException("Listener not initialized!");
            } else if (!listener.isActive()) {
                throw new InvalidStateException("Listener not active!");
            } else if (!listenerWatchDog.isServiceRunning()) {
                throw new InvalidStateException("Listener service not running!");
            }
        } catch (CouldNotPerformException ex) {
            throw new InvalidStateException("Listener of " + this + " not connected to middleware!", ex);
        }

        try {
            if (remoteServer == null) {
                throw new InvalidStateException("RemoteServer not initialized!");
            } else if (!remoteServer.isActive()) {
                throw new InvalidStateException("RemoteServer not active!");
            } else if (!remoteServerWatchDog.isServiceRunning()) {
                throw new InvalidStateException("RemoteServer service not running!");
            }
        } catch (CouldNotPerformException ex) {
            throw new InvalidStateException("RemoteServer of " + this + " not connected to middleware!", ex);
        }
    }

    public void validateData() throws InvalidStateException {
        if (!isDataAvailable()) {
            throw new InvalidStateException(this + " not synchronized yet!", new NotAvailableException("data"));
        }
    }

    public void waitForMiddleware() throws CouldNotPerformException, InterruptedException {
        if (listenerWatchDog == null) {
            throw new NotAvailableException("listenerWatchDog");
        }

        if (remoteServerWatchDog == null) {
            throw new NotAvailableException("remoteServiceWatchDog");
        }

        listenerWatchDog.waitForServiceActivation();
        remoteServerWatchDog.waitForServiceActivation();
    }

    /**
     * Method blocks until the remote reaches the desired connection state. In
     * case the timeout is expired an TimeoutException will be thrown.
     *
     * @param connectionState the desired connection state
     * @param timeout         the timeout in milliseconds until the method throw a
     *                        TimeoutException in case the connection state was not reached.
     * @throws InterruptedException                                is thrown in case the thread is externally
     *                                                             interrupted.
     * @throws org.openbase.jul.exception.TimeoutException         is thrown in case the
     *                                                             timeout is expired without reaching the connection state.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the connection state does not match and the shutdown of this remote
     *                                                             has been initialized
     */
    public void waitForConnectionState(final ConnectionState connectionState, long timeout) throws InterruptedException, TimeoutException, CouldNotPerformException {
        synchronized (connectionMonitor) {
            boolean delayDetected = false;
            while (!Thread.currentThread().isInterrupted()) {

                // check if requested connection state is reached.
                if (this.connectionState.equals(connectionState)) {
                    if (delayDetected) {
                        logger.info("Continue processing because " + getClass().getSimpleName().replace("Remote", "") + "[" + getScopeStringRep() + "] is now " + this.connectionState.name().toLowerCase() + ".");
                    }
                    return;
                }

                failOnShutdown("Waiting for connectionState[" + connectionState.name() + "] in connectionState[" + this.connectionState.name() + "] on shutdown");

                // detect delay for long term wait
                if (timeout == 0) {
                    connectionMonitor.wait(15000);
                    if (!this.connectionState.equals(connectionState)) {
                        failOnShutdown("Waiting for connectionState[" + connectionState.name() + "] in connectionState[" + this.connectionState.name() + "] on shutdown");
                        delayDetected = true;
                        logger.info("Wait for " + this.connectionState.name().toLowerCase() + " " + getClass().getSimpleName().replace("Remote", "") + "[" + getScopeStringRep() + "] to be " + connectionState.name().toLowerCase() + "...");
                        connectionMonitor.wait();
                    }
                    continue;
                }

                // wait till timeout
                connectionMonitor.wait(timeout);
                if (timeout != 0 && !this.connectionState.equals(connectionState)) {
                    throw new TimeoutException("Timeout expired!");
                }
            }
        }
    }

    private void failOnShutdown(String message) throws InvalidStateException {
        if (this.shutdownInitiated) {
            throw new InvalidStateException(message);
        }
    }

    private String getScopeStringRep() {
        try {
            return ScopeGenerator.generateStringRep(scope);
        } catch (CouldNotPerformException ex) {
            return "?";
        }
    }

    /**
     * Method blocks until the remote reaches the desired connection state.
     *
     * @param connectionState the desired connection state
     * @throws InterruptedException                                is thrown in case the thread is externally
     *                                                             interrupted.
     * @throws org.openbase.jul.exception.CouldNotPerformException is thrown in case the
     *                                                             the remote is not active and the waiting condition is based on ConnectionState CONNECTED or CONNECTING.
     */
    public void waitForConnectionState(final ConnectionState connectionState) throws InterruptedException, CouldNotPerformException {
        try {
            waitForConnectionState(connectionState, 0);
        } catch (TimeoutException ex) {
            assert false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public Scope getScope() throws NotAvailableException {
        if (scope == null) {
            throw new NotAvailableException("scope", new InvalidStateException("remote service not initialized yet!"));
        }
        return scope;
    }

    private class InternalUpdateHandler implements Handler {

        @Override
        public void internalNotify(Event event) {
            try {
                logger.debug("Internal notification: " + event.toString());
                Object dataUpdate = event.getData();

                if (dataUpdate == null) {
                    // do not set to connecting while reconnecting because when timed wrong this can cause
                    // reinit to cancel sync tasks and reinit while switch to connecting anyway when finished
                    if (getConnectionState() == ConnectionState.RECONNECTING) {
                        return;
                    }

                    ExceptionPrinter.printVerboseMessage("Remote connection to Controller[" + ScopeTransformer.transform(getScope()) + "] was detached because the controller shutdown was initiated.", logger);
                    setConnectionState(CONNECTING);
                    return;
                }

                // skip events which were send later than the last received update
                long userTime = RPCHelper.USER_TIME_VALUE_INVALID;
                if (event.getMetaData().hasUserTime(RPCHelper.USER_TIME_KEY)) {
                    userTime = event.getMetaData().getUserTime(RPCHelper.USER_TIME_KEY);
                } else {
                    logger.debug("Data message does not contain user time key on scope " + ScopeGenerator.generateStringRep(event.getScope()));
                }

                // filter outdated events
                if (event.getMetaData().getCreateTime() < newestEventTime || (event.getMetaData().getCreateTime() == newestEventTime && userTime < newestEventTimeNano)) {
                    logger.debug("Skip event on scope[" + ScopeGenerator.generateStringRep(event.getScope()) + "] because event seems to be outdated! Received event time < latest event time [" + event.getMetaData().getCreateTime() + "<= " + newestEventTime + "][" + event.getMetaData().getUserTime(RPCHelper.USER_TIME_KEY) + " < " + newestEventTimeNano + "]");
                    return;
                }

                if (userTime != RPCHelper.USER_TIME_VALUE_INVALID) {
                    newestEventTimeNano = userTime;
                }
                newestEventTime = event.getMetaData().getCreateTime();
                applyDataUpdate((M) dataUpdate);
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Internal notification failed!", ex), logger);
            }
        }

    }

    /**
     * Just a hack to support unit group remotes.
     * TODO: redesign needed.
     */
    protected void applyExternalDataUpdate(final M data) throws CouldNotPerformException {
        try {
            if (listenerWatchDog.isActive() && remoteServerWatchDog.isActive()) {
                throw new InvalidStateException("Because of synchronization reasons data updates can not be applied on active remote services.");
            }
        } catch (NullPointerException ex) {
            // does not care because remote should not be activated anyway.
        }
        applyDataUpdate(data);
    }

    /**
     * Method is used to internally update the data object.
     *
     * @param data
     */
    private void applyDataUpdate(final M data) {
        this.data = data;
        CompletableFuture<M> currentSyncFuture = null;
        Future<M> currentSyncTask = null;

        // Check if sync is in process.
        synchronized (syncMonitor) {
            if (syncFuture != null) {
                currentSyncFuture = syncFuture;
                currentSyncTask = syncTask;
                syncFuture = null;
                syncTask = null;
            }
        }

        if (currentSyncFuture != null) {
            currentSyncFuture.complete(data);
        }

        if (currentSyncTask != null && !currentSyncTask.isDone()) {
            currentSyncTask.cancel(false);
        }

        setConnectionState(CONNECTED);

        // Notify data update
        try {
            notifyDataUpdate(data);
            internalPriorizedDataObservable.notifyObservers(data);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update!", ex), logger);
        }

        try {
            dataObservable.notifyObservers(data);
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update to all observer!", ex), logger);
        }
    }

    /**
     * Overwrite this method to get informed about data updates.
     * This is now equivalent to adding an observer on the internalPriorizedDataObserver.
     *
     * @param data new arrived data messages.
     * @throws CouldNotPerformException
     */
    @Deprecated
    protected void notifyDataUpdate(M data) throws CouldNotPerformException {
        // dummy method, please overwrite if needed.
    }

    /**
     * This observable notifies sequentially and prior to the normal dataObserver.
     * It should be used by remote services which need to do some things before
     * external objects are notified.
     *
     * @return The internal priorized data observable.
     */
    protected Observable<M> getIntenalPriorizedDataObservable() {
        return internalPriorizedDataObservable;
    }

    /**
     * @param observer
     * @deprecated use addDataObserver(observer); instead!
     */
    @Deprecated
    public void addObserver(Observer<M> observer) {
        addDataObserver(observer);
    }

    /**
     * @param observer
     * @deprecated use removeDataObserver(observer); instead
     */
    @Deprecated
    public void removeObserver(Observer<M> observer) {
        removeDataObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addDataObserver(final Observer<M> observer) {
        dataObservable.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeDataObserver(final Observer<M> observer) {
        dataObservable.removeObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addConnectionStateObserver(final Observer<ConnectionState> observer) {
        connectionStateObservable.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeConnectionStateObserver(final Observer<ConnectionState> observer) {
        connectionStateObservable.removeObserver(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Future<Long> ping() {
        synchronized (pingLock) {
            if (pingTask == null || pingTask.isDone()) {
                pingTask = GlobalCachedExecutorService.submit(() -> {
                    try {
                        validateMiddleware();
                        Long requestTime = remoteServer.call("ping", System.currentTimeMillis(), JPService.testMode() ? PING_TEST_TIMEOUT : PING_TIMEOUT);
                        lastPingReceived = System.currentTimeMillis();
                        connectionPing = lastPingReceived - requestTime;
                        return connectionPing;
                    } catch (TimeoutException ex) {
                        synchronized (connectionMonitor) {
                            if (connectionState == CONNECTED) {
                                logger.warn("Remote connection to Controller[" + ScopeTransformer.transform(getScope()) + "] lost!");

                                // init reconnection
                                setConnectionState(CONNECTING);
                            }
                        }
                        throw ex;
                    } catch (final InvalidStateException ex) {
                        // reinit remote service because middleware connection lost!
                        switch (connectionState) {
                            // only if the connection was established before and no reconnect is ongoing.
                            case CONNECTING:
                            case CONNECTED:
                                try {
                                    reinit();
                                } catch (final CouldNotPerformException exx) {
                                    ExceptionPrinter.printHistory("Recovering middleware connection failed!", exx, logger);
                                }
                        }
                        throw ex;
                    } catch (CouldNotPerformException ex) {
                        throw new CouldNotPerformException("ping failed", ex);
                    }
                });
            }
            return pingTask;
        }
    }

    /**
     * Method returns the result of the latest connection ping between this
     * remote and its main controller.
     *
     * @return the latest connection delay in milliseconds.
     */
    @Override
    public Long getPing() {
        return connectionPing;
    }

    private void skipSyncTasks() {
        CompletableFuture<M> currentSyncFuture = null;
        Future<M> currentSyncTask = null;

        // Check if sync is in process.
        synchronized (syncMonitor) {
            if (syncFuture != null) {

                // only skip sync future if the shutdown was initiated!
                if (shutdownInitiated) {
                    currentSyncFuture = syncFuture;
                    syncFuture = null;
                }

                currentSyncTask = syncTask;
                syncTask = null;
            }
        }

        // Notify sync cancellation
        try {
            // skip if shutdown is in progress otherwise the sync future will be null
            if (currentSyncFuture != null) {
                currentSyncFuture.cancel(true);
            }
        } catch (CancellationException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not cancel synchronization because the cancellation was canceled!", ex), logger, LogLevel.WARN);
        }
        try {
            if (currentSyncTask != null) {
                currentSyncTask.cancel(true);
            }
        } catch (CancellationException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not cancel synchronization because the cancellation was canceled!", ex), logger, LogLevel.WARN);
        }
    }

    /**
     * Method prints a class instance representation.
     *
     * @return the class string representation.
     */
    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + "[scope:" + ScopeGenerator.generateStringRep(scope) + "]";
        } catch (CouldNotPerformException ex) {
            return getClass().getSimpleName() + "[scope:?]";
        }
    }

    private static long generateTimeout(long currentTimeout) {
        return Math.min(METHOD_CALL_MAX_TIMEOUT, (long) (currentTimeout * METHOD_CALL_TIMEOUT_MULTIPLIER + (JITTER_RANDOM.nextDouble() * 1000)));
    }
}
