/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBInformerInterface;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.Scope;
import rsb.config.ParticipantConfig;

/**
 *
 * @author Divine Threepwood
 * @param <DT>
 */
public class RSBSynchronizedInformer<DT extends Object> extends RSBSynchronizedParticipant<Informer<DT>> implements RSBInformerInterface<DT> {

    protected final Logger logger = LoggerFactory.getLogger(RSBSynchronizedInformer.class);

    private final Class<DT> type;


    /**
     * Creates an informer for a specific data type with a given scop..
     *
     * @param scope the scope
     * @param type the data type to send by this informer
     */
    protected RSBSynchronizedInformer(final Scope scope, final Class<DT> type) throws InstantiationException {
        this(scope, type, null);
    }

    /**
     * Creates an informer for a specific data type with a given scope and with
     * a specified config.
     *
     * @param scope the scope
     * @param type the data type to send by this informer
     * @param config
     */
    protected RSBSynchronizedInformer(final Scope scope, final Class<DT> type, final ParticipantConfig config) throws InstantiationException {
        super(scope, config);
        this.type = type;
    }

    @Override
    protected Informer<DT> init() throws InitializeException {
        synchronized (participantLock) {
            if (config == null) {
                return Factory.getInstance().createInformer(scope, type);
            } else {
                return Factory.getInstance().createInformer(scope, type, config);
            }
        }
    }

    @Override
    public Event send(final Event event) throws CouldNotPerformException {
        synchronized (participantLock) {
            try {
                if (event == null) {
                    throw new NotAvailableException("event");
                }
                return getParticipant().send(event);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not send Event[" + event + "]!", ex);
            }
        }
    }

    @Override
    public Event send(final DT data) throws CouldNotPerformException {
        synchronized (participantLock) {
            try {
                if (data == null) {
                    throw new NotAvailableException("data");
                }
                return getParticipant().send(data);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not send Data[" + data + "]!", ex);
            }
        }
    }

    @Override
    public Class<?> getTypeInfo() throws NotAvailableException {
        synchronized (participantLock) {
            try {
                return getParticipant().getTypeInfo();
            } catch (Exception ex) {
                throw new NotAvailableException("type info", ex);
            }
        }
    }

    @Override
    public void setTypeInfo(final Class<DT> typeInfo) throws CouldNotPerformException {
        synchronized (participantLock) {
            try {
                if (typeInfo == null) {
                    throw new NotAvailableException("typeInfo");
                }
                getParticipant().setTypeInfo(typeInfo);
            } catch (Exception ex) {
                throw new CouldNotPerformException("Could not set type info!", ex);
            }
        }
    }
}
