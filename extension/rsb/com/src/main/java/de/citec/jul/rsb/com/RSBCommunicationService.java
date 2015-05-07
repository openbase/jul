/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extention.protobuf.BuilderSyncSetup;
import de.citec.jul.extension.rsb.scope.ScopeProvider;
import com.google.protobuf.Descriptors;
import com.google.protobuf.GeneratedMessage;
import com.google.protobuf.GeneratedMessage.Builder;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InitializationException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Activatable;
import de.citec.jul.extension.rsb.com.RSBInformerInterface.InformerType;
import de.citec.jul.extension.rsb.scope.ScopeTransformer;
import de.citec.jul.schedule.WatchDog;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.ReadLock;
import java.util.concurrent.locks.ReentrantReadWriteLock.WriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Factory;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.Scope;
import rsb.patterns.Callback;
import rsb.patterns.LocalServer;
import rst.rsb.ScopeType;

/**
 *
 * @author mpohling
 * @param <M>
 * @param <MB>
 */
public abstract class RSBCommunicationService<M extends GeneratedMessage, MB extends M.Builder<MB>> implements ScopeProvider, Activatable {

    public enum ConnectionState {

        Online, Offline
    };

    public final static Scope SCOPE_SUFFIX_RPC = new Scope("/ctrl");
    public final static Scope SCOPE_SUFFIX_INFORMER = new Scope("/status");

    public final static String RPC_REQUEST_STATUS = "requestStatus";
    public final static Event RPC_SUCCESS = new Event(String.class, "Success");

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    protected RSBInformerInterface<M> informer;
    protected LocalServer server;
    protected WatchDog informerWatchDog;
    protected WatchDog serverWatchDog;

    private final MB dataBuilder;

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

    /**
     *
     * @param informerType
     * @throws InitializationException
     * @deprecated not used anymore because informer pooling is done by rsb core
     * since 0.12.
     */
    @Deprecated
    public void init(final InformerType informerType) throws InitializationException {
        init();
    }

    public void init() throws InitializationException {
        try {
            logger.debug("Init informer service...");
            this.informer = new RSBSingleInformer(scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_INFORMER)), detectMessageClass());
            informerWatchDog = new WatchDog(informer, "RSBInformer[" + scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_INFORMER)) + "]");
        } catch (InitializeException | InstantiationException ex) {
            throw new InitializationException(this, ex);
        }

        try {
            logger.info("Init rpc server...");
            // Get local server object which allows to expose remotely callable methods.
            server = Factory.getInstance().createLocalServer(scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_RPC)));

            // register rpc methods.
            server.addMethod(RPC_REQUEST_STATUS, new Callback() {

                @Override
                public Event internalInvoke(Event request) throws Throwable {

                    return new Event(detectMessageClass(), requestStatus());
                }
            });
            registerMethods(server);
            serverWatchDog = new WatchDog(server, "RSBLocalServer[" + scope.concat(new Scope(Scope.COMPONENT_SEPARATOR).concat(SCOPE_SUFFIX_RPC)) + "]");

        } catch (RSBException | InstantiationException ex) {
            throw new InitializationException(this, ex);
        }
    }

    protected Class<? extends M> detectMessageClass() {
        return (Class<? extends M>) ((M) cloneDataBuilder().buildPartial()).getClass();
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
        try {
            informer.deactivate();
        } catch (RSBException ex) {
            throw new AssertionError(ex);
        }
        serverWatchDog.deactivate();
        state = ConnectionState.Offline;
    }

    @Override
    public boolean isActive() {
        return informerWatchDog.isActive() && serverWatchDog.isActive();
    }

    public M getData() throws RSBException {
        try {
            return (M) cloneDataBuilder().build();
        } catch (Exception ex) {
            throw new RSBException("Could not build message!", ex);
        }
    }

    public MB cloneDataBuilder() {
        try {
            dataBuilderReadLock.lock();
            return (MB) dataBuilder.clone();
        } finally {
            dataBuilderReadLock.unlock();
        }
    }
    
    protected BuilderSyncSetup getBuilderSyncSetup() {
        return new BuilderSyncSetup(dataBuilder, dataBuilderReadLock, dataBuilderWriteLock);
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
     * {@code Code Example:
     *
     *     try (ClosableDataBuilder builder = getClosableDataBuilder()) {
     *         builder.builder.setMotionState(motion);
     *     } catch (Exception ex) {
     *         throw new CouldNotPerformException("Could not apply data change!", ex);
     *     }
     * }
     * </pre> In this example the ClosableDataBuilder.close method is be called
     * in background after leaving the try brackets.
     *
     * @return a new builder wrapper with a locked builder instance.
     */
    protected synchronized ClosableDataBuilder getClosableDataBuilder() {
        return new ClosableDataBuilder(dataBuilder);
    }

    public class ClosableDataBuilder implements java.lang.AutoCloseable {

        public MB builder;

        protected ClosableDataBuilder(MB builder) {
            this.builder = builder;
            dataBuilderReadLock.lock();
        }

        @Override
        public void close() throws Exception {
            dataBuilderReadLock.unlock();
        }
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    public void notifyChange() {
        logger.info("Notify change of " + this);
        try {
            informer.send(getData());
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not notify update", ex));
        }
    }

    protected final void setField(String name, Object value) {
        try {
            try {
                dataBuilderReadLock.lock();
                Descriptors.FieldDescriptor findFieldByName = dataBuilder.getDescriptorForType().findFieldByName(name);
                if (findFieldByName == null) {
                    throw new NotAvailableException("Field[" + name + "] does not exist for type " + dataBuilder.getClass().getName());
                }
                try {
                    dataBuilderWriteLock.lock();
                    dataBuilder.setField(findFieldByName, value);
                } finally {
                    dataBuilderWriteLock.unlock();
                }
            } finally {
                dataBuilderReadLock.unlock();
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

    public abstract void registerMethods(final LocalServer server) throws RSBException;

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + scope + "]";
    }
}
