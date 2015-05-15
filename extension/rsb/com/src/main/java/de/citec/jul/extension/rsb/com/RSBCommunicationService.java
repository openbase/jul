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
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InitializationException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.extension.protobuf.BuilderSyncSetup;
import de.citec.jul.extension.protobuf.ClosableDataBuilder;
import de.citec.jul.iface.Activatable;
import de.citec.jul.extension.rsb.scope.ScopeTransformer;
import de.citec.jul.iface.Changeable;
import de.citec.jul.schedule.WatchDog;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Scope;
import rsb.patterns.Callback;
import rst.rsb.ScopeType;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public abstract class RSBCommunicationService<M extends GeneratedMessage, MB extends M.Builder<MB>> implements ScopeProvider, Activatable, Changeable {

    public enum ConnectionState {

        Online, Offline
    };

    public final static Scope SCOPE_SUFFIX_CONTROL = new Scope("/ctrl");
    public final static Scope SCOPE_SUFFIX_STATUS = new Scope("/status");

    public final static String RPC_REQUEST_STATUS = "requestStatus";
    public final static Event RPC_SUCCESS = new Event(String.class, "Success");

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

    public RSBCommunicationService(final ScopeType.Scope scope, final MB builder) throws CouldNotTransformException, InstantiationException {
        this(ScopeTransformer.transform(scope), builder);
    }

    public RSBCommunicationService(final Scope scope, final MB builder) throws InstantiationException {
        logger.debug("Create RSBCommunicationService for component " + getClass().getSimpleName() + " on " + scope + ".");
        this.dataBuilder = builder;

        try {
            if (builder == null) {
                throw new NotAvailableException("builder");
            }

            if (scope == null) {
                throw new NotAvailableException("scope");
            }

            this.scope = new Scope(scope.toString().toLowerCase());
            this.dataLock = new ReentrantReadWriteLock();
            this.dataBuilderReadLock = dataLock.readLock();
            this.dataBuilderWriteLock = dataLock.writeLock();
            this.messageClass = detectMessageClass();
            this.server = new NotInitializedRSBLocalServer();
            this.informer = new NotInitializedRSBInformer<>();
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    public RSBCommunicationService(final String name, final String lable, final ScopeProvider location, final MB builder) throws InstantiationException, CouldNotPerformException {
        this(generateScope(name, lable, location), builder);
    }

    public static Scope generateScope(final String name, final String label, final ScopeProvider location) throws CouldNotPerformException {
        try {
            return location.getScope().concat(new Scope(Scope.COMPONENT_SEPARATOR + name).concat(new Scope(Scope.COMPONENT_SEPARATOR + label)));
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Coult not generate scope!", ex);
        }
    }

    public void init() throws InitializationException {
        try {
            logger.debug("Init informer service...");
            this.informer = new RSBSynchronizedInformer<M>(scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_STATUS)), messageClass);
            informerWatchDog = new WatchDog(informer, "RSBInformer[" + scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_STATUS)) + "]");

            logger.info("Init rpc server...");
            // Get local server object which allows to expose remotely callable methods.
            server = RSBFactory.getInstance().createSynchronizedLocalServer(scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_CONTROL)));

            // register rpc methods.
            server.addMethod(RPC_REQUEST_STATUS, new Callback() {

                @Override
                public Event internalInvoke(Event request) throws Throwable {

                    return new Event(messageClass, requestStatus());
                }
            });
            registerMethods(server);
            serverWatchDog = new WatchDog(server, "RSBLocalServer[" + scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_CONTROL)) + "]");

        } catch (CouldNotPerformException | NullPointerException ex) {
            throw new InitializationException(this, ex);
        }
    }

    private Class<M> detectMessageClass() throws CouldNotPerformException {
        try {
            Class<M> clazz = (Class<M>) dataBuilder.getClass().getEnclosingClass();
            if (clazz == null) {
                throw new NotAvailableException("message class");
            }
            return clazz;
//            return (Class<M>) ((M) cloneDataBuilder().buildPartial()).getClass();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not detect message class of builder " + dataBuilder.getClass().getName() + "!", ex);
        }
    }

    public Class<M> getMessageClass() {
        return messageClass;
    }

    @Override
    public void activate() {
        logger.debug("Activate RSBCommunicationService for: " + this);
        informerWatchDog.activate();
        serverWatchDog.activate();
        state = ConnectionState.Online;
    }

    @Override
    public void deactivate() throws InterruptedException {
        informerWatchDog.deactivate();
        serverWatchDog.deactivate();
        state = ConnectionState.Offline;
    }

    @Override
    public boolean isActive() {
        return informerWatchDog.isActive() && serverWatchDog.isActive();
    }

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
        return new BuilderSyncSetup<MB>(dataBuilder, dataBuilderReadLock, dataBuilderWriteLock, this);
    }

    /**
     * This method generates a closable data builder wrapper including the
     * internal builder instance. Be informed that the internal builder is
     * directly locked and all internal builder operations are queued. In fact
     * call the close method soon as possible to release the builder lock after
     * you builder modifications, otherwise the overall processing pipeline is
     * delayed.
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
     * </pre> In this example the ClosableDataBuilder.close method is be called
     * in background after leaving the try brackets.
     *
     * @return a new builder wrapper with a locked builder instance.
     */
    public synchronized ClosableDataBuilder<MB> getDataBuilder(final Object consumer) {
        return new ClosableDataBuilder<MB>(getBuilderSetup(), consumer);
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public void notifyChange() throws CouldNotPerformException {
        logger.info("Notify change of " + this);
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

    public M requestStatus() throws CouldNotPerformException {
        try {
            notifyChange();
            return getData();
        } catch (Exception ex) {
            throw ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not request status update.", ex));
        }
    }

    public abstract void registerMethods(final RSBLocalServerInterface server) throws CouldNotPerformException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + scope + "]";
    }
}
