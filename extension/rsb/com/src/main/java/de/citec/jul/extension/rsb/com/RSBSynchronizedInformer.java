/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.schedule.SyncObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
import rsb.Scope;

/**
 *
 * @author Divine Threepwood
 * @param <DataType>
 */
public class RSBSynchronizedInformer<DataType extends Object> implements RSBInformerInterface<DataType> {

    protected final Logger logger = LoggerFactory.getLogger(RSBSynchronizedInformer.class);

    private Informer<DataType> internalInformer;
    private final SyncObject internalInformerLock = new SyncObject("internalInformer");
    private final Scope scope;
    private final Class<DataType> type;

    /**
     * Creates an informer for a specific data type with a given scope and with
     * a specified config.
     *
     * @param scope the scope
     * @param type the data type to send by this informer
     * @throws InitializeException error initializing the informer
     */
    public RSBSynchronizedInformer(final String scope, final Class<DataType> type) throws InitializeException {
        this(new Scope(scope), type);
    }

    /**
     * Creates an informer for a specific data type with a given scope and with
     * a specified config.
     *
     * @param scope the scope
     * @param type the data type to send by this informer
     * @throws InitializeException error initializing the informer
     */
    public RSBSynchronizedInformer(final Scope scope, final Class<DataType> type) throws InitializeException {
        this.scope = scope;
        this.type = type;
    }

    private void initInformer() throws InitializeException {
        synchronized (internalInformerLock) {
            this.internalInformer = Factory.getInstance().createInformer(scope, type);
        }
    }

    @Override
    public Event send(Event event) throws CouldNotPerformException {
        synchronized (internalInformerLock) {
            try {
                if (event == null) {
                    throw new NotAvailableException("event");
                }
                if (internalInformer == null) {
                    throw new NotAvailableException("internalInformer");
                }
                return internalInformer.send(event);
            } catch (NotAvailableException | RSBException | NullPointerException ex) {
                throw new CouldNotPerformException("Could not send Event[" + event + "]!", ex);
            }
        }
    }

    @Override
    public Event send(DataType data) throws CouldNotPerformException {
        synchronized (internalInformerLock) {
            try {
                if (internalInformer == null) {
                    throw new NotAvailableException("internalInformer");
                }
                return internalInformer.send(data);
            } catch (NotAvailableException | RSBException | NullPointerException ex) {
                throw new CouldNotPerformException("Could not send Data[" + data + "]!", ex);
            }
        }
    }

    @Override
    public Class<?> getTypeInfo() throws NotAvailableException {
        synchronized (internalInformerLock) {
            try {
                if (internalInformer == null) {
                    throw new NotAvailableException("internalInformer");
                }
                return internalInformer.getTypeInfo();
            } catch (Exception ex) {
                throw new NotAvailableException("type info", ex);
            }
        }
    }

    @Override
    public void setTypeInfo(Class<DataType> typeInfo) throws CouldNotPerformException {
        synchronized (internalInformerLock) {
            try {
                if (internalInformer == null) {
                    throw new NotAvailableException("internalInformer");
                }
                internalInformer.setTypeInfo(typeInfo);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not set type info!", ex);
            }
        }
    }

    @Override
    public Scope getScope() {
        return scope;
    }

    @Override
    public void activate() throws CouldNotPerformException {
        try {
            synchronized (internalInformerLock) {
                if (internalInformer == null) {
                    initInformer();
                }
//            System.out.println("service[" + this.hashCode() + ":" + internalInformer.isActive() + "] activate");
                internalInformer.activate();
//            System.out.println("service[" + this.hashCode() + ":" + internalInformer.isActive() + "] activated.");
            }
        } catch (RSBException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not deactivate informer!", ex);
        }
    }

    @Override
    public void deactivate() throws CouldNotPerformException, InterruptedException {
        try {
            synchronized (internalInformerLock) {

                if (internalInformer == null) {
                    logger.warn("Ignore informer deactivation because informer does not exist!");
                    return;
                }

//            System.out.println("service[" + this.hashCode() + ":" + internalInformer.isActive() + "] deactivate.");
                internalInformer.deactivate();
//            System.out.println("service[" + this.hashCode() + ":" + internalInformer.isActive() + "] deactivate: " + internalInformer.isActive());
                internalInformer = null;
            }
        } catch (RSBException | InterruptedException | NullPointerException ex) {
            throw new CouldNotPerformException("Could not deactivate informer!", ex);
        }
    }

    @Override
    public boolean isActive() {
        synchronized (internalInformerLock) {
            if (internalInformer == null) {
                return false;
            }
            return internalInformer.isActive();
        }
    }
}
