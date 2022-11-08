package org.openbase.jul.communication.controller;

/*
 * #%L
 * JUL Extension Controller
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
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

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Any;
import com.google.protobuf.Descriptors;
import org.openbase.jul.annotation.RPCMethod;
import org.openbase.jul.communication.config.CommunicatorConfig;
import org.openbase.jul.communication.iface.CommunicatorFactory;
import org.openbase.jul.communication.iface.Publisher;
import org.openbase.jul.communication.iface.RPCServer;
import org.openbase.jul.communication.mqtt.CommunicatorFactoryImpl;
import org.openbase.jul.communication.mqtt.DefaultCommunicatorConfig;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.*;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.*;
import org.openbase.jul.extension.protobuf.BuilderSyncSetup.NotificationStrategy;
import org.openbase.jul.extension.type.iface.ScopeProvider;
import org.openbase.jul.extension.type.iface.TransactionIdProvider;
import org.openbase.jul.extension.type.processing.ScopeProcessor;
import org.openbase.jul.iface.Pingable;
import org.openbase.jul.iface.Readyable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.pattern.controller.MessageController;
import org.openbase.jul.pattern.provider.DataProvider;
import org.openbase.jul.schedule.*;
import org.openbase.type.communication.EventType;
import org.openbase.type.communication.EventType.Event;
import org.openbase.type.communication.ScopeType.Scope;
import org.openbase.type.communication.mqtt.PrimitiveType.Primitive;
import org.openbase.type.domotic.state.AvailabilityStateType.AvailabilityState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static org.openbase.jul.iface.Shutdownable.registerShutdownHook;
import static org.openbase.type.domotic.state.AvailabilityStateType.AvailabilityState.State.*;

/**
 * @param <M>  the message type of the communication service
 * @param <MB> the builder for message M
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */

public abstract class AbstractControllerServer<M extends AbstractMessage, MB extends M.Builder<MB>> implements MessageController<M, MB>, ScopeProvider, DataProvider<M>, Readyable, TransactionIdProvider {

    public final static String SCOPE_ELEMENT_SUFFIX_CONTROL = "/ctrl";
    public final static String SCOPE_ELEMENT_SUFFIX_STATUS = "/status";

    public final static Scope SCOPE_SUFFIX_CONTROL = ScopeProcessor.generateScope(SCOPE_ELEMENT_SUFFIX_CONTROL);
    public final static Scope SCOPE_SUFFIX_STATUS = ScopeProcessor.generateScope(SCOPE_ELEMENT_SUFFIX_STATUS);

    private static final long NOTIFICATION_TIMEOUT = TimeUnit.SECONDS.toMillis(15);

    public final static String RPC_REQUEST_STATUS = "requestStatus";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final ShutdownDaemon shutdownDaemon;

    protected Publisher publisher;
    protected RPCServer server;
    protected WatchDog publisherWatchDog;
    protected WatchDog serverWatchDog;

    private final MB dataBuilder;
    private final Class<M> messageClass;

    private final SyncObject transactionIdLock = new SyncObject(getClass());

    protected final ReentrantReadWriteLock dataLock;
    private final BundledReentrantReadWriteLock manageLock;
    private final ReentrantReadWriteLock.ReadLock dataBuilderReadLock;
    private final ReentrantReadWriteLock.WriteLock dataBuilderWriteLock;

    protected Scope scope;

    private final SyncObject controllerAvailabilityMonitor = new SyncObject("ControllerAvailabilityMonitor");
    private AvailabilityState.State availabilityState;
    private volatile boolean initialized, destroyed;

    private final MessageObservable dataObserver;
    private Future initialDataSyncFuture;

    private volatile long transaction_id = 0;

    private final CommunicatorFactory factory = CommunicatorFactoryImpl.Companion.getInstance();
    private final CommunicatorConfig defaultCommunicatorConfig = DefaultCommunicatorConfig.Companion.getInstance();

    /**
     * Create a communication service.
     *
     * @param builder the initial data builder
     *
     * @throws InstantiationException if the creation fails
     */
    public AbstractControllerServer(final MB builder) throws InstantiationException {
        logger.debug("Create AbstractControllerServer for component " + getClass().getSimpleName() + ".");
        this.dataBuilder = builder;

        try {
            if (builder == null) {
                throw new NotAvailableException("builder");
            }

            this.availabilityState = AvailabilityState.State.OFFLINE;
            this.dataLock = new ReentrantReadWriteLock();
            this.dataBuilderReadLock = dataLock.readLock();
            this.dataBuilderWriteLock = dataLock.writeLock();
            this.manageLock = new BundledReentrantReadWriteLock(dataLock, true, this);
            this.messageClass = detectDataClass();
            this.dataObserver = new MessageObservable(this);
            this.dataObserver.setExecutorService(GlobalCachedExecutorService.getInstance().getExecutorService());
            this.initialized = false;
            this.destroyed = false;
            // todo: manage shutdown via unit controller registry to avoid multiple thread creation during shutdown
            this.shutdownDaemon = registerShutdownHook(this, getShutdownDelay());
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * Method can be overwritten to delay the controller shutdown.
     *
     * @return the delay in milliseconds.
     */
    protected long getShutdownDelay() {
        return 0;
    }


    /**
     * @param scope
     *
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final Scope scope) throws InitializationException, InterruptedException {
        init(scope, defaultCommunicatorConfig);
    }

    /**
     * @param scope
     *
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final String scope) throws InitializationException, InterruptedException {
        try {
            init(ScopeProcessor.generateScope(scope));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     * @param scope
     * @param communicatorConfig
     *
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final Scope scope, final CommunicatorConfig communicatorConfig) throws InitializationException, InterruptedException {
        manageLock.lockWriteInterruptibly(this);
        try {
            final boolean alreadyActivated = isActive();
            CommunicatorConfig internalCommunicatorConfig = communicatorConfig;

            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            // check if this instance was partly or fully initialized before.
            if (initialized | publisherWatchDog != null | serverWatchDog != null) {
                deactivate();
                reset();
            }

            this.scope = scope;

            final String scopeStringRep = ScopeProcessor.generateStringRep(scope).toLowerCase();
            final Scope internalScope = ScopeProcessor.generateScope(scopeStringRep);

            // init new instances.
            logger.debug("Init AbstractControllerServer for component " + getClass().getSimpleName() + " on " + scopeStringRep);
            publisher = factory.createPublisher(ScopeProcessor.concat(internalScope, SCOPE_SUFFIX_STATUS), internalCommunicatorConfig);
            publisherWatchDog = new WatchDog(publisher, "Publisher[" + ScopeProcessor.generateStringRep(publisher.getScope()) + "]");

            // get rpc server object which allows to expose remotely callable methods.
            server = factory.createRPCServer(ScopeProcessor.concat(internalScope, SCOPE_SUFFIX_CONTROL), internalCommunicatorConfig);

            // register rpc methods.
            registerMethods(server);

            // register default methods
            try {
                server.registerMethods(Pingable.class, this);
            } catch (InvalidStateException ex) {
                // if already registered then everything is fine and we can continue...
            }
            try {
                server.registerMethods((Class) getClass(), this);
            } catch (InvalidStateException /*| NoSuchMethodException*/ ex) {
                // if already registered then everything is fine, and we can continue...
            }

            serverWatchDog = new WatchDog(server, "RPCServer[" + ScopeProcessor.generateStringRep(server.getScope()) + "]");

            this.publisherWatchDog.addObserver((final WatchDog source, WatchDog.ServiceState data) -> {
                if (data == WatchDog.ServiceState.RUNNING) {

                    // Sync data after service start.
                    initialDataSyncFuture = GlobalCachedExecutorService.submit(() -> {
                        try {
                            // skip if shutdown was already initiated
                            if (publisherWatchDog.isServiceDone() || serverWatchDog.isServiceDone()) {
                                return;
                            }

                            publisherWatchDog.waitForServiceActivation();
                            serverWatchDog.waitForServiceActivation();

                            // mark controller as online.
                            setAvailabilityState(ONLINE);

                            logger.debug("trigger initial sync");
                            notifyChange();
                        } catch (InterruptedException ex) {
                            Thread.currentThread().interrupt();
                        } catch (CouldNotPerformException ex) {
                            if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not trigger data sync!", ex), logger, LogLevel.ERROR);
                            }
                        }
                    });
                }
            });

            postInit();

            initialized = true;

            // check if communication service was already activated before and recover state.
            if (alreadyActivated) {
                activate();
            }
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        } finally {
            manageLock.unlockWrite(this);
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

    private Class<M> detectDataClass() throws CouldNotPerformException {
        try {
            @SuppressWarnings("unchecked")
            Class<M> clazz = (Class<M>) dataBuilder.getClass().getEnclosingClass();
            if (clazz == null) {
                throw new NotAvailableException("message class");
            }
            return clazz;
        } catch (SecurityException | NotAvailableException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not detect message class of builder " + dataBuilder.getClass().getName() + "!", ex);
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Class<M> getDataClass() {
        return messageClass;
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException     {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        manageLock.lockWriteInterruptibly(this);
        try {
            validateInitialization();
            logger.debug("Activate AbstractControllerServer for: " + this);
            setAvailabilityState(ACTIVATING);
            assert serverWatchDog != null;
            assert publisherWatchDog != null;
            serverWatchDog.activate();
            publisherWatchDog.activate();
        } finally {
            manageLock.unlockWrite(this);
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
        manageLock.lockWriteInterruptibly(this);
        try {
            try {
                validateInitialization();
            } catch (InvalidStateException ex) {
                // was never initialized!
                return;
            }

            // skip initial data sync if still running
            if (initialDataSyncFuture != null && !initialDataSyncFuture.isDone()) {
                initialDataSyncFuture.cancel(true);
            }

            logger.debug("Deactivate AbstractControllerServer for: " + this);
            // The order is important: The publisher publishes a zero event when the availabilityState is set to deactivating which leads remotes to disconnect
            // The remotes try to reconnect again and start a requestData. If the server is still active it will respond
            // and the remotes will think that the server is still there.
            if (serverWatchDog != null) {
                serverWatchDog.deactivate();
            }
            // inform remotes about deactivation
            setAvailabilityState(DEACTIVATING);
            if (publisherWatchDog != null) {
                publisherWatchDog.deactivate();
            }
            setAvailabilityState(OFFLINE);
        } finally {
            manageLock.unlockWrite(this);
        }
    }

    private void reset() {
        manageLock.lockWrite(this);
        try {
            // clear init flag
            initialized = false;

            if (serverWatchDog != null) {
                serverWatchDog.shutdown();
                serverWatchDog = null;
            }

            // clear existing instances.        
            if (publisherWatchDog != null) {
                publisherWatchDog.shutdown();
                publisherWatchDog = null;
            }
        } finally {
            manageLock.unlockWrite(this);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        try {
            deactivate();
        } catch (final CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not deactivate " + this + " during shutdown!", ex, logger);
        } catch (final InterruptedException ex) {
            logger.debug("Deactivation of " + this + " skipped because of interruption. Shutdown will be continued...");
        }
        reset();
        destroyed = true;

        if (shutdownDaemon != null) {
            shutdownDaemon.cancel();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isActive() {
        try {
            validateInitialization();
        } catch (InvalidStateException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
        return publisherWatchDog.isActive() && serverWatchDog.isActive();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws NotAvailableException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public M getData() throws NotAvailableException {
        try {
            return (M) cloneDataBuilder().build();
        } catch (Exception ex) {
            throw new NotAvailableException("Data", new CouldNotPerformException("Could not build message!", ex));
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public Future<M> getDataFuture() {
        try {
            return FutureProcessor.completedFuture(getData());
        } catch (NotAvailableException ex) {
            CompletableFuture<M> future = new CompletableFuture<>();
            future.completeExceptionally(ex);
            return future;
        }
    }

    /**
     * @param controllerAvailability
     *
     * @throws InterruptedException
     */
    private void setAvailabilityState(final AvailabilityState.State controllerAvailability) throws InterruptedException {
        synchronized (controllerAvailabilityMonitor) {

            // filter unchanged events
            if (this.availabilityState.equals(controllerAvailability)) {
                return;
            }

            // update state and notify
            this.availabilityState = controllerAvailability;
            logger.debug(this + " is now " + controllerAvailability.name());

            try {
                // notify remotes about controller shutdown
                if (availabilityState.equals(DEACTIVATING)) {
                    try {
                        validateInitialization();
                        if (!publisher.isActive()) {
                            logger.debug("Skip update notification because connection not established.");
                            return;
                        }
                        try {
                            publisher.publish(EventType.Event.newBuilder().build(), true);
                        } catch (CouldNotPerformException ex) {
                            throw new CouldNotPerformException("Could not notify change of " + this + "!", ex);
                        }
                    } catch (final NotInitializedException ex) {
                        logger.debug("Skip update notification because instance is not initialized.");
                    } catch (CouldNotPerformException ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Could not update communication service state in internal data object!", ex), logger);
                    }
                }
            } finally {
                // wakeup subscriber.
                this.controllerAvailabilityMonitor.notifyAll();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void waitForAvailabilityState(final AvailabilityState.State availabilityState) throws InterruptedException {
        synchronized (controllerAvailabilityMonitor) {
            while (!Thread.currentThread().isInterrupted()) {
                if (this.availabilityState.equals(availabilityState)) {
                    return;
                }
                controllerAvailabilityMonitor.wait();
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public MB cloneDataBuilder() {
        dataBuilderReadLock.lock();
        try {
            return dataBuilder.clone();
        } finally {
            dataBuilderReadLock.unlock();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public BuilderSyncSetup<MB> getBuilderSetup() {
        return new BuilderSyncSetup<>(dataBuilder, dataBuilderReadLock, dataBuilderWriteLock, this);
    }

    /**
     * {@inheritDoc}
     *
     * @param consumer {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    @Deprecated
    public ClosableDataBuilder<MB> getDataBuilder(final Object consumer) {
        return new ClosableDataBuilderImpl<>(getBuilderSetup(), consumer);
    }

    /**
     * {@inheritDoc}
     *
     * @param consumer             {@inheritDoc}
     * @param notificationStrategy {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    @Deprecated
    public ClosableDataBuilder<MB> getDataBuilder(final Object consumer, final NotificationStrategy notificationStrategy) {
        return new ClosableDataBuilderImpl<>(getBuilderSetup(), consumer, notificationStrategy);
    }

    /**
     * {@inheritDoc}
     *
     * @param consumer {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public ClosableDataBuilder<MB> getDataBuilderInterruptible(final Object consumer) throws InterruptedException {
        return new ClosableInterruptibleDataBuilderImpl<>(getBuilderSetup(), consumer);
    }

    /**
     * {@inheritDoc}
     *
     * @param consumer             {@inheritDoc}
     * @param notificationStrategy {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public ClosableDataBuilder<MB> getDataBuilderInterruptible(final Object consumer, final NotificationStrategy notificationStrategy) throws InterruptedException {
        return new ClosableInterruptibleDataBuilderImpl<>(getBuilderSetup(), consumer, notificationStrategy);
    }

    /**
     * This method generates a closable manager lock wrapper.
     * Be informed that the controller and all its services are
     * directly locked and internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the lock after
     * you are done, otherwise the overall processing pipeline is delayed.
     * <p>
     * Note: Be aware that your access is time limited an the lock will auto released if locked in longer term.
     * This is a recovering feature but should never be used by design!
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try(final CloseableWriteLockWrapper ignored = getManageWriteLock(this)) {
     *         // do important stuff...
     *     }
     * }
     * </pre> In this example the CloseableWriteLockWrapper.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer a responsible instance which consumes the lock.
     *
     * @return a new builder wrapper which already locks the manage lock.
     */
    protected CloseableWriteLockWrapper getManageWriteLock(final Object consumer) {
        return new CloseableWriteLockWrapper(new BundledReentrantReadWriteLock(manageLock, true, consumer));
    }

    /**
     * This method generates a closable manager lock wrapper.
     * Be informed that the controller and all its services are
     * directly locked and internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the lock after
     * you are done, otherwise the overall processing pipeline is delayed.
     * <p>
     * Note: Be aware that your access is time limited an the lock will auto released if locked in longer term.
     * This is a recovering feature but should never be used by design!
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try(final CloseableReadLockWrapper ignored = getManageReadLock(this)) {
     *         // do important stuff...
     *     }
     * }
     * </pre> In this example the CloseableWriteLockWrapper.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer a responsible instance which consumes the lock.
     *
     * @return a new builder wrapper which already locks the manage lock.
     */
    protected CloseableReadLockWrapper getManageReadLock(final Object consumer) {
        return new CloseableReadLockWrapper(new BundledReentrantReadWriteLock(manageLock, true, consumer));
    }

    /**
     * This method generates a closable manager lock wrapper.
     * Be informed that the controller and all its services are
     * directly locked and internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the lock after
     * you are done, otherwise the overall processing pipeline is delayed.
     * <p>
     * Note: Be aware that your access is time limited an the lock will auto released if locked in longer term.
     * This is a recovering feature but should never be used by design!
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try(final CloseableInterruptibleWriteLockWrapper ignored = getManageWriteLock(this)) {
     *         // do important stuff...
     *     }
     * }
     * </pre> In this example the CloseableWriteLockWrapper.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer a responsible instance which consumes the lock.
     *
     * @return a new builder wrapper which already locks the manage lock.
     * @throws InterruptedException in case the thread was externally interrupted during the locking.
     */
    protected CloseableWriteLockWrapper getManageWriteLockInterruptible(final Object consumer) throws InterruptedException {
        return new CloseableInterruptibleWriteLockWrapper(new BundledReentrantReadWriteLock(manageLock, true, consumer));
    }

    /**
     * This method generates a closable manager lock wrapper.
     * Be informed that the controller and all its services are
     * directly locked and internal builder operations are queued. Therefore please
     * call the close method soon as possible to release the lock after
     * you are done, otherwise the overall processing pipeline is delayed.
     * <p>
     * Note: Be aware that your access is time limited an the lock will auto released if locked in longer term.
     * This is a recovering feature but should never be used by design!
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try(final CloseableInterruptibleReadLockWrapper ignored = getManageReadLock(this)) {
     *         // do important stuff...
     *     }
     * }
     * </pre> In this example the CloseableWriteLockWrapper.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer a responsible instance which consumes the lock.
     *
     * @return a new builder wrapper which already locks the manage lock.
     * @throws InterruptedException in case the thread was externally interrupted during the locking.
     */
    protected CloseableReadLockWrapper getManageReadLockInterruptible(final Object consumer) throws InterruptedException {
        return new CloseableInterruptibleReadLockWrapper(new BundledReentrantReadWriteLock(manageLock, true, consumer));
    }

    /**
     * This method generates a closable lock provider.
     * Be informed that the controller and all its services are
     * directly locked and internal builder operations are queued. Therefore please
     * release the locks after usage, otherwise the overall processing pipeline is
     * delayed.
     * <p>
     * Note: Be aware that your access is time limited an the lock will auto released if locked in longer term.
     * This is a recovering feature but should never be used by design!
     *
     * @return a provider to access the manage read and write lock.
     */
    protected CloseableLockProvider getManageLock() {
        return new CloseableLockProvider() {

            @Override
            public CloseableReadLockWrapper getCloseableReadLock(final Object consumer) {
                return getManageReadLock(consumer);
            }

            @Override
            public CloseableWriteLockWrapper getCloseableWriteLock(final Object consumer) {
                return getManageWriteLock(consumer);
            }
        };
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws NotAvailableException {@inheritDoc}
     */
    @Override
    public Scope getScope() throws NotAvailableException {
        if (scope == null) {
            throw new NotAvailableException("scope", new InvalidStateException("communication service not initialized yet!"));
        }
        return scope;
    }

    /**
     * Synchronize all registered remote instances about a data change.
     *
     * @throws CouldNotPerformException
     * @throws java.lang.InterruptedException
     */
    @Override
    public void notifyChange() throws CouldNotPerformException, InterruptedException {
        logger.debug("Notify data change of {}", this);
        // synchronized by manageable lock to prevent reinit between validateInitialization and publish
        M newData;
        manageLock.lockWriteInterruptibly(this);
        try {
            try {
                validateInitialization();
            } catch (final NotInitializedException ex) {
                // only forward if instance was not destroyed before otherwise skip notification.
                if (destroyed) {
                    return;
                }
                throw ex;
            }

            // update the current data builder before updating to allow implementations to change data beforehand
            newData = updateDataToPublish(cloneDataBuilder());
            final Event event = Event.newBuilder()
                    .setPayload(Any.pack(newData))
                    .build();

            // only publish if controller is active
            if (isActive()) {
                try {
                    waitForMiddleware(NOTIFICATION_TIMEOUT, TimeUnit.MILLISECONDS);
                    publisher.publish(event, true);
                } catch (TimeoutException ex) {
                    if (ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                        throw ex;
                    }
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Skip data update notification because middleware is not ready since " + TimeUnit.MILLISECONDS.toSeconds(NOTIFICATION_TIMEOUT) + " seconds of " + this + "!", ex), logger, LogLevel.WARN);
                } catch (CouldNotPerformException ex) {
                    if (ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                        throw ex;
                    }
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform about data change of " + this + "!", ex), logger);
                }
            }

            // this is disabled because the order is mixed up and its not true!
            // validate that no locks are write locked by the same thread during notification in order to avoid deadlocks.
//            if (manageLock.isAnyWriteLockHeldByCurrentThread()) {
//                throw new VerificationFailedException("Could not guarantee controller state read access during notification. This can potentially lead to deadlocks during the notification process in case controller states are accessed by any observation routines!");
//            }

            // Notify data update internally
            try {
                notifyDataUpdate(newData);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify data update!", ex), logger);
            }

            // Notify data update to all observer
            dataObserver.notifyObservers(newData);
        } finally {
            manageLock.unlockWrite(this);
        }
    }

    /**
     * Called before publishing data via the publisher. Can be implemented by
     * sub classes to update data which can be received by everyone.
     *
     * @param dataBuilder a clone of the current data builder.
     *
     * @return a message build from the data builder
     *
     * @throws CouldNotPerformException if the update fails
     */
    protected M updateDataToPublish(MB dataBuilder) throws CouldNotPerformException {
        return (M) dataBuilder.build();
    }

    /**
     * Overwrite this method to get informed about data updates.
     *
     * @param data new arrived data messages.
     *
     * @throws CouldNotPerformException
     */
    protected void notifyDataUpdate(M data) throws CouldNotPerformException {
        // dummy method, please overwrite if needed.
    }

    /**
     * @param fieldNumber
     * @param value
     *
     * @throws CouldNotPerformException
     */
    protected final void setDataField(int fieldNumber, Object value) throws CouldNotPerformException {
        try {
            try {
                dataBuilderWriteLock.lock();
                Descriptors.FieldDescriptor findFieldByName = dataBuilder.getDescriptorForType().findFieldByNumber(fieldNumber);
                if (findFieldByName == null) {
                    throw new NotAvailableException("Field[" + fieldNumber + "] does not exist for type " + dataBuilder.getClass().getName());
                }
                dataBuilder.setField(findFieldByName, value);
            } finally {
                dataBuilderWriteLock.unlock();
            }
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not set field [" + fieldNumber + "=" + value + "] for " + this, ex);
        }
    }

    /**
     * @param fieldName
     * @param value
     *
     * @throws CouldNotPerformException
     */
    protected final void setDataField(String fieldName, Object value) throws CouldNotPerformException {
        try {
            try {
                dataBuilderWriteLock.lock();
                Descriptors.FieldDescriptor findFieldByName = dataBuilder.getDescriptorForType().findFieldByName(fieldName);
                if (findFieldByName == null) {
                    throw new NotAvailableException("Field[" + fieldName + "] does not exist for type " + dataBuilder.getClass().getName());
                }

                dataBuilder.setField(findFieldByName, value);
            } finally {
                dataBuilderWriteLock.unlock();
            }
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not set field [" + fieldName + "=" + value + "] for " + this, ex);
        }
    }

    /**
     * @param name
     *
     * @return
     *
     * @throws NotAvailableException
     */
    protected final Object getDataField(String name) throws NotAvailableException {
        try {
            MB dataClone = cloneDataBuilder();
            Descriptors.FieldDescriptor findFieldByName = dataClone.getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + name + "] does not exist for type " + dataClone.getClass().getName());
            }
            return dataClone.getField(findFieldByName);
        } catch (Exception ex) {
            throw new NotAvailableException(name, this, ex);
        }
    }

    /**
     * @param name
     *
     * @return
     *
     * @throws CouldNotPerformException
     */
    protected final boolean hasDataField(final String name) throws CouldNotPerformException {
        try {
            MB dataClone = cloneDataBuilder();
            Descriptors.FieldDescriptor findFieldByName = dataClone.getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                return false;
            }
            return dataClone.hasField(findFieldByName);
        } catch (Exception ex) {
            return false;
        }
    }

    /**
     * @param name
     *
     * @return
     *
     * @throws CouldNotPerformException
     */
    protected final boolean supportsDataField(final String name) throws CouldNotPerformException {
        try {
            Descriptors.FieldDescriptor findFieldByName = dataBuilder.getDescriptorForType().findFieldByName(name);
            return findFieldByName != null;
        } catch (NullPointerException ex) {
            return false;
        }
    }

    /**
     * @param fieldId
     *
     * @return
     */
    protected final Descriptors.FieldDescriptor getDataFieldDescriptor(int fieldId) {
        return cloneDataBuilder().getDescriptorForType().findFieldByNumber(fieldId);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public AvailabilityState.State getControllerAvailabilityState() {
        return availabilityState;
    }


    /**
     * This method validates the controller initialisation.
     *
     * @throws NotInitializedException is thrown if the controller is not initialized.
     * @throws InterruptedException    is thrown in case the thread was externally interrupted.
     */
    public void validateInitialization() throws NotInitializedException, InterruptedException {
        manageLock.lockReadInterruptibly(this);
        try {
            if (!initialized) {
                if (shutdownDaemon.isShutdownInProgress()) {
                    throw new NotInitializedException("server", new ShutdownInProgressException("server"));
                }
                throw new NotInitializedException("server");
            }
        } finally {
            manageLock.unlockRead(this);
        }
    }

    /**
     * This method validates the controller activation.
     *
     * @throws InvalidStateException is thrown if the controller is not active.
     */
    public void validateActivation() throws InvalidStateException {
        if (isShutdownInProgress()) {
            throw new InvalidStateException(new ShutdownInProgressException(this));
        } else if (!isActive()) {
            throw new InvalidStateException(this + " not activated!");
        }
    }

    public void validateMiddleware() throws InvalidStateException {
        validateActivation();
        if (publisher == null || !publisher.isActive() || !publisherWatchDog.isServiceRunning()) {
            throw new InvalidStateException("Publisher of " + this + " not connected to middleware!");
        }

        if (server == null || !server.isActive() || !serverWatchDog.isServiceRunning()) {
            throw new InvalidStateException("Server of " + this + " not connected to middleware!");
        }
    }

    public void waitForMiddleware(final long timeout, final TimeUnit timeUnit) throws InterruptedException, CouldNotPerformException {
        final TimeoutSplitter timeSplit = new TimeoutSplitter(timeout, timeUnit);
        validateActivation();
        publisherWatchDog.waitForServiceActivation(timeSplit.getTime(), TimeUnit.MILLISECONDS);
        serverWatchDog.waitForServiceActivation(timeSplit.getTime(), TimeUnit.MILLISECONDS);
    }

    /**
     * Method checks if the requesting thread holds the data builder write lock of this unit.
     * This check can help to decide if a notification should be performed or not since releasing
     * the write lock mostly notifies anyway depending on the selected notification strategy.
     *
     * @return true if the current thread hold the builder write lock, otherwise false.
     */
    public boolean isDataBuilderWriteLockedByCurrentThread() {
        return dataBuilderWriteLock.isHeldByCurrentThread();
    }

    /**
     * {@inheritDoc }
     *
     * @param timestamp {@inheritDoc }
     *
     * @return {@inheritDoc }
     */
    @Override
    public Future<Long> ping(final Long timestamp) {
        try {
            validateMiddleware();
        } catch (InvalidStateException e) {
            try {
                waitForMiddleware(1, TimeUnit.SECONDS);
            } catch (InterruptedException ex) {
                Thread.currentThread().interrupt();
                FutureProcessor.canceledFuture(Long.class, ex);
            } catch (CouldNotPerformException ex) {
                // controller not ready to respond.
                FutureProcessor.canceledFuture(Long.class, ex);
            }
        }
        return FutureProcessor.completedFuture(timestamp);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     *
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     */
    @RPCMethod
    @Override
    public M requestStatus() throws CouldNotPerformException {
        logger.trace("requestStatus of {}", this);
        try {
            return getData();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Could not request status update.", ex), logger, LogLevel.ERROR);
        }
    }

    /**
     * Register methods for RPCs on the internal RPC server.
     *
     * @param server the rpc server on which the methods should be registered.
     *
     * @throws CouldNotPerformException if registering methods fails
     */
    public abstract void registerMethods(final RPCServer server) throws CouldNotPerformException;

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public boolean isDataAvailable() {
        // for controllers the data object is always available since it is created during startup.
        return true;
    }

    /**
     * Method returns true if this instance was initialized, activated and is successfully connected to the middleware.
     *
     * @return returns true if this instance is ready otherwise false.
     */
    @Override
    public Boolean isReady() {
        try {
            validateInitialization();
            validateActivation();
            validateMiddleware();
            return true;
        } catch (InvalidStateException ex) {
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return false;
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException     {@inheritDoc}
     */
    @Override
    public void waitForData() throws CouldNotPerformException, InterruptedException {
        // because this is the controller, the data is already available.
    }

    /**
     * {@inheritDoc}
     *
     * @param timeout  {@inheritDoc}
     * @param timeUnit {@inheritDoc}.
     *
     * @throws CouldNotPerformException {@inheritDoc}
     * @throws InterruptedException     {@inheritDoc}
     */
    @Override
    public void waitForData(long timeout, TimeUnit timeUnit) throws CouldNotPerformException, InterruptedException {
        // because this is the controller, the data is already available.
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void addDataObserver(Observer<DataProvider<M>, M> observer) {
        dataObserver.addObserver(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @param observer {@inheritDoc}
     */
    @Override
    public void removeDataObserver(Observer<DataProvider<M>, M> observer) {
        dataObserver.removeObserver(observer);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public long getTransactionId() {
        synchronized (transactionIdLock) {
            return transaction_id;
        }
    }

    /**
     * Method generates a new id which can be used for the next transaction (e.g. state transition).
     *
     * @return the next transaction identifier.
     */
    public long generateTransactionId() {
        synchronized (transactionIdLock) {
            // Transaction id should never be 0 because thats the builder default value.
            logger.trace("increment transaction id from {} to {}", transaction_id, (transaction_id + 1));
            return ++transaction_id;
        }
    }

    /**
     * Methde sets a new generated transaction id in the global data field of this controller.
     *
     * @throws CouldNotPerformException
     */
    public void updateTransactionId() throws CouldNotPerformException {
        // we need to lock the data builder lock first to avoid deadlocks because its needed anyway when the id is updated.
        try {
            dataBuilderWriteLock.lockInterruptibly();
            try {
                synchronized (transactionIdLock) {
                    setDataField(TransactionIdProvider.TRANSACTION_ID_FIELD_NAME, generateTransactionId());
                }
            } finally {
                dataBuilderWriteLock.unlock();
            }
        } catch (InterruptedException ex) {
            // recover interruption
            Thread.currentThread().interrupt();
        }
    }

    @Override
    public void validateData() throws InvalidStateException {

        if (isShutdownInProgress()) {
            throw new InvalidStateException(this + " not synchronized!", new ShutdownInProgressException(this));
        }

        if (!isDataAvailable()) {
            throw new InvalidStateException(this + " not synchronized yet!", new NotAvailableException("data"));
        }
    }

    /**
     * Method returns true if this instance is currently shutting down.
     */
    public boolean isShutdownInProgress() {
        return shutdownDaemon.isShutdownInProgress();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     */
    @Override
    public String toString() {
        if (publisher == null) {
            return getClass().getSimpleName();
        }
        return getClass().getSimpleName() + "[" + publisher.getScope() + "]";
    }
}
