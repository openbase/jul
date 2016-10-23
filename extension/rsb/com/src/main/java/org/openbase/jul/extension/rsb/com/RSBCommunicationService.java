package org.openbase.jul.extension.rsb.com;

/*
 * #%L
 * JUL Extension RSB Communication
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Future;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.openbase.jps.core.JPService;
import org.openbase.jps.exception.JPServiceException;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.CouldNotTransformException;
import org.openbase.jul.exception.InitializationException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.InvalidStateException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.NotInitializedException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.extension.protobuf.BuilderSyncSetup;
import org.openbase.jul.extension.protobuf.ClosableDataBuilder;
import org.openbase.jul.extension.protobuf.MessageController;
import org.openbase.jul.extension.rsb.com.jp.JPRSBTransport;
import org.openbase.jul.extension.rsb.iface.RSBInformer;
import org.openbase.jul.extension.rsb.iface.RSBLocalServer;
import org.openbase.jul.extension.rsb.scope.ScopeGenerator;
import org.openbase.jul.extension.rsb.scope.ScopeTransformer;
import org.openbase.jul.extension.rst.iface.ScopeProvider;
import org.openbase.jul.iface.Pingable;
import org.openbase.jul.iface.Requestable;
import static org.openbase.jul.iface.Shutdownable.registerShutdownHook;
import org.openbase.jul.pattern.Controller.ControllerAvailabilityState;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.Observer;
import org.openbase.jul.schedule.GlobalExecutionService;
import org.openbase.jul.schedule.SyncObject;
import org.openbase.jul.schedule.WatchDog;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <M> the message type of the communication service
 * @param <MB> the builder for message M
 */
public abstract class RSBCommunicationService<M extends GeneratedMessage, MB extends M.Builder<MB>> implements MessageController<M, MB>, ScopeProvider {

    static {
        RSBSharedConnectionConfig.load();
    }

    public final static rsb.Scope SCOPE_SUFFIX_CONTROL = new rsb.Scope("/ctrl");
    public final static rsb.Scope SCOPE_SUFFIX_STATUS = new rsb.Scope("/status");

    public final static String RPC_REQUEST_STATUS = "requestStatus";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RSBInformer<Object> informer;
    protected RSBLocalServer server;
    protected WatchDog informerWatchDog;
    protected WatchDog serverWatchDog;

    private final MB dataBuilder;
    private final Class<M> messageClass;

    private final ReentrantReadWriteLock dataLock;
    private final ReadLock dataBuilderReadLock;
    private final WriteLock dataBuilderWriteLock;

    protected Scope scope;

    private final SyncObject controllerAvailabilityMonitor = new SyncObject("ControllerAvailabilityMonitor");
    private ControllerAvailabilityState controllerAvailabilityState;
    private boolean initialized;

    /**
     * Create a communication service.
     *
     * @param builder the initial data builder
     * @throws InstantiationException if the creation fails
     */
    public RSBCommunicationService(final MB builder) throws InstantiationException {
        logger.debug("Create RSBCommunicationService for component " + getClass().getSimpleName() + ".");
        this.dataBuilder = builder;

        try {
            if (builder == null) {
                throw new NotAvailableException("builder");
            }

            this.controllerAvailabilityState = ControllerAvailabilityState.OFFLINE;
            this.dataLock = new ReentrantReadWriteLock();
            this.dataBuilderReadLock = dataLock.readLock();
            this.dataBuilderWriteLock = dataLock.writeLock();
            this.messageClass = detectDataClass();
            this.server = new NotInitializedRSBLocalServer();
            this.informer = new NotInitializedRSBInformer<>();
            this.initialized = false;
            registerShutdownHook(this);

        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     *
     * @param scope
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    /**
     *
     * @param scope
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final rsb.Scope scope) throws InitializationException, InterruptedException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    /**
     *
     * @param scope
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final String scope) throws InitializationException, InterruptedException {
        try {
            init(new rsb.Scope(scope));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     *
     * @param label
     * @param type
     * @param location
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final String label, final String type, final ScopeProvider location) throws InitializationException, InterruptedException {
        try {
            init(ScopeGenerator.generateScope(label, type, location.getScope()));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    /**
     *
     * @param scope
     * @param participantConfig
     * @throws InitializationException
     * @throws InterruptedException
     */
    public void init(final rsb.Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
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

    /**
     *
     * @param scope
     * @param participantConfig
     * @throws InitializationException
     * @throws InterruptedException
     */
    public synchronized void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException, InterruptedException {
        try {
            final boolean alreadyActivated = isActive();
            ParticipantConfig internalParticipantConfig = participantConfig;

            try {
                // activate transport communication set by the JPRSBTransport property.
                enableTransport(internalParticipantConfig, JPService.getProperty(JPRSBTransport.class).getValue());
            } catch (JPServiceException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
            }

            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            // check if this instance was partly or fully initialized before.
            if (initialized | informerWatchDog != null | serverWatchDog != null) {
                deactivate();
                reset();
            }

            this.scope = scope;
            rsb.Scope internalScope = new rsb.Scope(ScopeGenerator.generateStringRep(scope).toLowerCase());

            // init new instances.
            logger.debug("Init RSBCommunicationService for component " + getClass().getSimpleName() + " on " + internalScope + ".");
            informer = new RSBSynchronizedInformer<>(internalScope.concat(new rsb.Scope(rsb.Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_STATUS)), Object.class, internalParticipantConfig);
            informerWatchDog = new WatchDog(informer, "RSBInformer[" + internalScope.concat(new rsb.Scope(rsb.Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_STATUS)) + "]");

            // get local server object which allows to expose remotely callable methods.
            server = RSBFactoryImpl.getInstance().createSynchronizedLocalServer(internalScope.concat(new rsb.Scope(rsb.Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_CONTROL)), internalParticipantConfig);

            // register rpc methods.
            RPCHelper.registerInterface(Pingable.class, this, server);
            RPCHelper.registerInterface(Requestable.class, this, server);
            registerMethods(server);

            serverWatchDog = new WatchDog(server, "RSBLocalServer[" + internalScope.concat(new rsb.Scope(rsb.Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_CONTROL)) + "]");

            this.serverWatchDog.addObserver(new Observer<WatchDog.ServiceState>() {

                @Override
                public void update(final Observable<WatchDog.ServiceState> source, WatchDog.ServiceState data) throws Exception {
                    if (data == WatchDog.ServiceState.RUNNING) {

                        setControllerAvailabilityState(ControllerAvailabilityState.ONLINE);

                        // Sync data after service start.
                        GlobalExecutionService.submit(() -> {
                            try {
                                serverWatchDog.waitForActivation();
                                logger.debug("trigger initial sync");
                                notifyChange();
                            } catch (InterruptedException | CouldNotPerformException ex) {
                                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not trigger data sync!", ex), logger, LogLevel.ERROR);
                            }
                        });
                    }
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
        } catch (Exception ex) {
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
     * @throws InterruptedException {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        validateInitialization();
        logger.debug("Activate RSBCommunicationService for: " + this);
        setControllerAvailabilityState(ControllerAvailabilityState.ACTIVATING);
        informerWatchDog.activate();
        serverWatchDog.activate();
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        try {
            validateInitialization();
        } catch (InvalidStateException ex) {
            // was never initialized!
            return;
        }
        // clear existing instances.
        setControllerAvailabilityState(ControllerAvailabilityState.DEACTIVATING);
        if (informerWatchDog != null) {
            informerWatchDog.deactivate();
        }
        if (serverWatchDog != null) {
            serverWatchDog.deactivate();
        }
        setControllerAvailabilityState(ControllerAvailabilityState.OFFLINE);
    }

    public void reset() {
        // clear existing instances.
        if (informerWatchDog != null) {
            informerWatchDog.shutdown();
            informerWatchDog = null;
            informer = new NotInitializedRSBInformer<>();
        }
        if (serverWatchDog != null) {
            serverWatchDog.shutdown();
            serverWatchDog = null;
            server = new NotInitializedRSBLocalServer();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void shutdown() {
        try {
            deactivate();
        } catch (CouldNotPerformException | InterruptedException ex) {
            ExceptionPrinter.printHistory("Could not deactivate " + this + " during shutdown!", ex, logger);
        }
        reset();
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
        }
        return informerWatchDog.isActive() && serverWatchDog.isActive();
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws CouldNotPerformException {@inheritDoc}
     */
    @SuppressWarnings("unchecked")
    @Override
    public M getData() throws CouldNotPerformException {
        try {
            return (M) cloneDataBuilder().build();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not build message!", ex);
        }
    }

    /**
     *
     * @param controllerAvailability
     * @throws InterruptedException
     */
    private void setControllerAvailabilityState(final ControllerAvailabilityState controllerAvailability) throws InterruptedException {
        synchronized (controllerAvailabilityMonitor) {

            // filter unchanged events
            if (this.controllerAvailabilityState.equals(controllerAvailability)) {
                return;
            }

            // update state and notify
            this.controllerAvailabilityState = controllerAvailability;
            logger.debug(this + " is now " + controllerAvailability.name());

            // notify remotes about controller shutdown
            if (controllerAvailabilityState.equals(ControllerAvailabilityState.DEACTIVATING)) {
                try {
                    logger.debug("Notify data change of " + this);
                    validateInitialization();
                    if (!informer.isActive()) {
                        logger.debug("Skip update notification because connection not established.");
                        return;
                    }
                    try {
                        informer.publish(new Event(informer.getScope(), Void.class, null));
                    } catch (CouldNotPerformException ex) {
                        throw new CouldNotPerformException("Could not notify change of " + this + "!", ex);
                    }
                } catch (CouldNotPerformException ex) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not update communication service state in internal data object!", ex), logger);
                }
            }

            // wakeup listener.
            this.controllerAvailabilityMonitor.notifyAll();
        }
    }

    /**
     * {@inheritDoc}
     *
     * @throws InterruptedException {@inheritDoc}
     */
    @Override
    public void waitForAvailabilityState(final ControllerAvailabilityState controllerAvailabilityState) throws InterruptedException {
        synchronized (controllerAvailabilityMonitor) {
            while (!Thread.currentThread().isInterrupted()) {
                if (this.controllerAvailabilityState.equals(controllerAvailabilityState)) {
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
        try {
            dataBuilderReadLock.lock();
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
     * @return {@inheritDoc}
     */
    @Override
    public synchronized ClosableDataBuilder<MB> getDataBuilder(final Object consumer) {
        return new ClosableDataBuilder<>(getBuilderSetup(), consumer);
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
        logger.debug("Notify data change of " + this);
        validateInitialization();
        if (!informer.isActive()) {
            logger.debug("Skip update notification because connection not established.");
            return;
        }
        try {
            informer.publish(getData());
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not notify change of " + this + "!", ex);
        }
    }

    /**
     *
     * @param fieldNumber
     * @param value
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
     *
     * @param fieldName
     * @param value
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
     *
     * @param name
     * @return
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
     *
     * @param name
     * @return
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
     *
     * @param name
     * @return
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
     *
     * @param fieldId
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
    public ControllerAvailabilityState getControllerAvailabilityState() {
        return controllerAvailabilityState;
    }

    /**
     *
     * @throws NotInitializedException
     */
    public void validateInitialization() throws NotInitializedException {
        if (!initialized) {
            throw new NotInitializedException("communication service");
        }
    }

    /**
     * Method can be used to calculate connection ping.
     * The given timestamp argument is just returned from the local server to calculate the delay on client side.
     *
     * @param timestemp
     * @return
     */
    @Override
    public Future<Long> ping(Long timestemp) {
        return CompletableFuture.completedFuture(timestemp);
    }

    /**
     * {@inheritDoc}
     *
     * @return {@inheritDoc}
     * @throws org.openbase.jul.exception.CouldNotPerformException {@inheritDoc}
     */
    @Override
    public M requestStatus() throws CouldNotPerformException {
        logger.debug("requestStatus of " + this);
        try {
            return getData();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Could not request status update.", ex), logger, LogLevel.ERROR);
        }
    }

    /**
     *
     * @param server
     * @throws CouldNotPerformException
     */
    public abstract void registerMethods(final RSBLocalServer server) throws CouldNotPerformException;

    @Override
    public String toString() {
        try {
            return getClass().getSimpleName() + "[" + informer.getScope().toString() + "]";
        } catch (NotAvailableException ex) {
            return getClass().getSimpleName() + "[]";
        }
    }
}
