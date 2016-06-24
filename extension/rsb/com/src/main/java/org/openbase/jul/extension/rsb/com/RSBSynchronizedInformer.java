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
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.rsb.iface.RSBInformerInterface;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import rsb.Event;
import rsb.Factory;
import rsb.Informer;
import rsb.InitializeException;
import rsb.RSBException;
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
     * @throws org.openbase.jul.exception.InstantiationException
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
     * @throws org.openbase.jul.exception.InstantiationException
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
    public Event publish(final Event event) throws CouldNotPerformException, InterruptedException {
        synchronized (participantLock) {
            logger.debug("Event[scope=" + event.getScope() + ", type=" + event.getType() + ", metaData=" + event.getMetaData() + "]");
            try {
                if (event == null) {
                    throw new NotAvailableException("event");
                }
                return getParticipant().publish(event);
            } catch (IllegalStateException ex) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Fatal error occured!", ex), logger);
            } catch (CouldNotPerformException | RSBException ex) {
                throw new CouldNotPerformException("Could not publish Event[scope=" + event.getScope() + ", type=" + event.getType() + ", metaData=" + event.getMetaData() + "]!", ex);
            }
        }
    }

    @Override
    public Event publish(final DT data) throws CouldNotPerformException, InterruptedException {
        synchronized (participantLock) {
            try {
                return getParticipant().publish(data);
            } catch (IllegalStateException ex) {
                throw ExceptionPrinter.printHistoryAndReturnThrowable(new CouldNotPerformException("Fatal error occured!", ex), logger);
            } catch (CouldNotPerformException | RSBException ex) {
                throw new CouldNotPerformException("Could not publish Data[" + data + "]!", ex);
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
