/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBInformerInterface;
import de.citec.jul.extension.rsb.iface.RSBLocalServerInterface;
import de.citec.jul.extension.rsb.scope.ScopeProvider;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPTestMode;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.InitializationException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.NotInitializedException;
import de.citec.jul.extension.protobuf.BuilderSyncSetup;
import de.citec.jul.extension.protobuf.ClosableDataBuilder;
import de.citec.jul.iface.Activatable;
import de.citec.jul.extension.rsb.scope.ScopeTransformer;
import de.citec.jul.iface.Changeable;
import de.citec.jul.schedule.WatchDog;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.config.ParticipantConfig;
import rsb.config.TransportConfig;
import rst.rsb.ScopeType;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public abstract class RSBCommunicationService<M extends GeneratedMessage, MB extends M.Builder<MB>> implements ScopeProvider, Activatable, Changeable {

    static {
        RSBSharedConnectionConfig.load();
    }

    public enum ConnectionState {

        Online, Offline
    };

    public final static Scope SCOPE_SUFFIX_CONTROL = new Scope("/ctrl");
    public final static Scope SCOPE_SUFFIX_STATUS = new Scope("/status");

    public final static String RPC_REQUEST_STATUS = "requestStatus";

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RSBInformerInterface<M> informer;
    protected RSBLocalServerInterface server;
    protected WatchDog informerWatchDog;
    protected WatchDog serverWatchDog;

    private final MB dataBuilder;
    private final Class<M> messageClass;

    private final ReentrantReadWriteLock dataLock;
    private final ReadLock dataBuilderReadLock;
    private final WriteLock dataBuilderWriteLock;

    protected Scope scope;
    private ConnectionState state;
    private boolean initialized;

    /**
     *
     * @param scope
     * @param builder
     * @throws InstantiationException
     * @deprecated setup context via init method.
     */
    @Deprecated
    public RSBCommunicationService(final Scope scope, final MB builder) throws InstantiationException {
        this(builder);
        try {
            this.scope = new Scope(scope.toString().toLowerCase());
        } catch (Exception ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * @param scope
     * @param builder
     * @throws de.citec.jul.exception.InstantiationException
     * @deprecated setup context via init method.
     */
    @Deprecated
    public RSBCommunicationService(final ScopeType.Scope scope, final MB builder) throws InstantiationException {
        this(builder);
        try {
            this.scope = ScopeTransformer.transform(scope);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public RSBCommunicationService(final MB builder) throws InstantiationException {
        logger.debug("Create RSBCommunicationService for component " + getClass().getSimpleName() + ".");
        this.dataBuilder = builder;

        try {
            if (builder == null) {
                throw new NotAvailableException("builder");
            }

            this.dataLock = new ReentrantReadWriteLock();
            this.dataBuilderReadLock = dataLock.readLock();
            this.dataBuilderWriteLock = dataLock.writeLock();
            this.messageClass = detectMessageClass();
            this.server = new NotInitializedRSBLocalServer();
            this.informer = new NotInitializedRSBInformer<>();
            this.initialized = false;
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    /**
     * @param label
     * @param type
     * @param location
     * @param builder
     * @throws de.citec.jul.exception.InstantiationException
     * @deprecated setup context via init method.
     */
    @Deprecated
    public RSBCommunicationService(final String label, final String type, final ScopeProvider location, final MB builder) throws InstantiationException, CouldNotPerformException {
        this(builder);
        try {
            this.scope = generateScope(label, type, location);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public static Scope generateScope(final String label, final String type, final ScopeProvider location) throws CouldNotPerformException {
        try {
            return location.getScope().concat(new Scope(Scope.COMPONENT_SEPARATOR + type).concat(new Scope(Scope.COMPONENT_SEPARATOR + label)));
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Coult not generate scope!", ex);
        }
    }

    public void init(final String scope) throws InitializationException {
        try {
            init(new Scope(scope));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void init(final ScopeType.Scope scope) throws InitializationException {
        try {
            init(ScopeTransformer.transform(scope));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void init(final String label, final ScopeProvider location) throws InitializationException {
        try {
            init(generateScope(label, getClass().getSimpleName(), location));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void init(final String label, final String type, final ScopeProvider location) throws InitializationException {
        try {
            init(generateScope(label, type, location));
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    public void init(final Scope scope) throws InitializationException {
        init(scope, RSBSharedConnectionConfig.getParticipantConfig());
    }

    public void init(final Scope scope, final ParticipantConfig participantConfig) throws InitializationException {

        ParticipantConfig internalParticipantConfig = participantConfig;
        
        // activate inprocess communication for junit tests.
        if (JPService.getProperty(JPTestMode.class).getValue()) {
            for (Map.Entry<String, TransportConfig> transport : internalParticipantConfig.getTransports().entrySet()) {
                transport.getValue().setEnabled(false);
            }
            internalParticipantConfig.getOrCreateTransport("inprocess").setEnabled(true);
        }

        try {
            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            Scope internalScope = new Scope(scope.toString().toLowerCase());

            logger.debug("Init RSBCommunicationService for component " + getClass().getSimpleName() + " on " + internalScope + ".");
            this.informer = new RSBSynchronizedInformer<>(internalScope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_STATUS)), messageClass, internalParticipantConfig);
            informerWatchDog = new WatchDog(informer, "RSBInformer[" + internalScope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_STATUS)) + "]");

            // Get local server object which allows to expose remotely callable methods.
            server = RSBFactory.getInstance().createSynchronizedLocalServer(internalScope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_CONTROL)), internalParticipantConfig);

            // register rpc methods.
            server.addMethod(RPC_REQUEST_STATUS, (Event request) -> new Event(messageClass, requestStatus()));
            registerMethods(server);
            serverWatchDog = new WatchDog(server, "RSBLocalServer[" + internalScope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_CONTROL)) + "]");
            initialized = true;
        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    private Class<M> detectMessageClass() throws CouldNotPerformException {
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

    public Class<M> getMessageClass() {
        return messageClass;
    }

    @Override
    public void activate() throws InterruptedException, CouldNotPerformException {
        checkInitialization();
        logger.debug("Activate RSBCommunicationService for: " + this);
        informerWatchDog.activate();
        serverWatchDog.activate();
        state = ConnectionState.Online;
    }

    @Override
    public void deactivate() throws InterruptedException, CouldNotPerformException {
        checkInitialization();
        informerWatchDog.deactivate();
        serverWatchDog.deactivate();
        state = ConnectionState.Offline;
    }

    @Override
    public boolean isActive() {
        try {
            checkInitialization();
        } catch (InvalidStateException ex) {
            return false;
        }
        return informerWatchDog.isActive() && serverWatchDog.isActive();
    }

    @SuppressWarnings("unchecked")
    public M getData() throws CouldNotPerformException {
        try {
            return (M) cloneDataBuilder().build();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not build message!", ex);
        }
    }

    public MB cloneDataBuilder() {
        try {
            dataBuilderReadLock.lock();
            return dataBuilder.clone();
        } finally {
            dataBuilderReadLock.unlock();
        }
    }

    protected BuilderSyncSetup<MB> getBuilderSetup() {
        return new BuilderSyncSetup<>(dataBuilder, dataBuilderReadLock, dataBuilderWriteLock, this);
    }

    /**
     * This method generates a closable data builder wrapper including the internal builder instance. Be informed that the internal builder is directly locked and all internal builder operations are
     * queued. In fact call the close method soon as possible to release the builder lock after you builder modifications, otherwise the overall processing pipeline is delayed.
     *
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try (ClosableDataBuilder<MotionSensor.Builder> dataBuilder = getDataBuilder(this)) {
     *         dataBuilder.getInternalBuilder().setMotionState(motion);
     *     } catch (Exception ex) {
     *         throw new CouldNotPerformException("Could not apply data change!", ex);
     *     }
     * }
     * </pre> In this example the ClosableDataBuilder.close method is be called in background after leaving the try brackets.
     *
     * @param consumer
     * @return a new builder wrapper with a locked builder instance.
     */
    public synchronized ClosableDataBuilder<MB> getDataBuilder(final Object consumer) {
        return new ClosableDataBuilder<>(getBuilderSetup(), consumer);
    }

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
     */
    @Override
    public void notifyChange() throws CouldNotPerformException {
        logger.debug("Notify change of " + this);
        checkInitialization();
        if (!informer.isActive()) {
            logger.debug("Skip update notification because connection not established.");
            return;
        }
        try {
            informer.send(getData());
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not notify change of " + this + "!", ex);
        }
    }

    protected final void setField(String name, Object value) {
        try {
            try {
                dataBuilderWriteLock.lock();
                Descriptors.FieldDescriptor findFieldByName = dataBuilder.getDescriptorForType().findFieldByName(name);
                if (findFieldByName == null) {
                    throw new NotAvailableException("Field[" + name + "] does not exist for type " + dataBuilder.getClass().getName());
                }

                dataBuilder.setField(findFieldByName, value);
            } finally {
                dataBuilderWriteLock.unlock();
            }
        } catch (Exception ex) {
            logger.warn("Could not set field [" + name + "=" + value + "] for " + this, ex);
        }
    }

    protected final Object getField(String name) throws CouldNotPerformException {
        try {
            MB dataClone = cloneDataBuilder();
            Descriptors.FieldDescriptor findFieldByName = dataClone.getDescriptorForType().findFieldByName(name);
            if (findFieldByName == null) {
                throw new NotAvailableException("Field[" + name + "] does not exist for type " + dataClone.getClass().getName());
            }
            return dataClone.getField(findFieldByName);
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not return value of field [" + name + "] for " + this, ex);
        }
    }

    protected final Descriptors.FieldDescriptor getFieldDescriptor(int fieldId) {
        return cloneDataBuilder().getDescriptorForType().findFieldByNumber(fieldId);
    }

    public ConnectionState getState() {
        return state;
    }

    private void checkInitialization() throws NotInitializedException {
        if (!initialized) {
            throw new NotInitializedException("communication service");
        }
    }

    public M requestStatus() throws CouldNotPerformException {
        try {
            return getData();
        } catch (Exception ex) {
            throw ExceptionPrinter.printHistoryAndReturnThrowable(logger, new CouldNotPerformException("Could not request status update.", ex));
        }
    }

    public abstract void registerMethods(final RSBLocalServerInterface server) throws CouldNotPerformException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + scope + "]";
    }
}
