/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.MultiException;
import org.dc.jul.pattern.ObservableImpl;
import org.dc.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Activatable;
import org.dc.jul.pattern.Observable;

/**
 *
 * @author mpohling
 */
public class WatchDog implements Activatable {

    private final Object EXECUTION_LOCK = new Object();
    private final Object activationLock;

    private static final long DELAY = 5000;

    public enum ServiceState {

        Unknown, Constructed, Initializing, Running, Terminating, Finished, Failed
    };

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final Activatable service;
    private final String serviceName;
    private Minder minder;
    private ServiceState serviceState = ServiceState.Unknown;

    private final ObservableImpl<ServiceState> serviceStateObserable;

    public WatchDog(final Activatable task, final String serviceName) throws InstantiationException {
        try {

            this.service = task;
            this.serviceName = serviceName;
            this.serviceStateObserable = new ObservableImpl<>();
            this.activationLock = new SyncObject(serviceName + "WatchDogLock");

            if (task == null) {
                throw new NotAvailableException("task");
            }

            Runtime.getRuntime().addShutdownHook(new Thread() {

                @Override
                public void run() {
                    try {
                        deactivate();
                    } catch (InterruptedException ex) {
                        logger.error("Could not shutdown " + serviceName + "!", ex);
                    }
                }
            });

            setServiceState(ServiceState.Constructed);
        } catch (CouldNotPerformException ex) {
            throw new InstantiationException(this, ex);
        }
    }

    @Override
    public void activate() throws InterruptedException {
        logger.trace("Try to activate service: " + serviceName);
        synchronized (EXECUTION_LOCK) {
            logger.trace("Init activation of service: " + serviceName);
            if (minder != null) {
                logger.debug("Skip activation, Service[" + serviceName + "] already running!");
                return;
            }
            minder = new Minder(serviceName + "WatchDog");
            logger.trace("Start activation of service: " + serviceName);
            minder.start();
        }

        try {
            waitForActivation();
        } catch (InterruptedException ex) {
            logger.warn("Could not wait for service activation!", ex);
            throw ex;
        }
    }

    @Override
    public void deactivate() throws InterruptedException {
        logger.trace("Try to deactivate service: " + serviceName);
        synchronized (EXECUTION_LOCK) {
            logger.trace("Init deactivation of service: " + serviceName);
            if (minder == null) {
                logger.debug("Skip deactivation, Service[" + serviceName + "] not running!");
                return;
            }

            logger.trace("Init service interruption...");
            minder.interrupt();
            logger.trace("Wait for service interruption...");
            minder.join();
            minder = null;
            logger.trace("Service interrupted!");
            skipActivation();
        }
    }

    @Override
    public boolean isActive() {
        return minder != null;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void waitForActivation() throws InterruptedException {

        synchronized (activationLock) {
            if (serviceState == ServiceState.Running) {
                return;
            }

            addObserver(new Observer<ServiceState>() {

                @Override
                public void update(final Observable<ServiceState> source, ServiceState data) throws Exception {
                    if (data == ServiceState.Running) {
                        synchronized (activationLock) {
                            activationLock.notifyAll();
                        }
                    }
                }
            });
            activationLock.wait();
        }
    }

    public void skipActivation() {
        synchronized (activationLock) {
            activationLock.notifyAll();
        }
    }

    private class Minder extends Thread {

        private Minder(String name) {
            super(name);
            setServiceState(ServiceState.Initializing);
        }

        @Override
        public void run() {
            try {
                try {
                    while (!isInterrupted()) {
                        if (!service.isActive()) {
                            setServiceState(ServiceState.Initializing);
                            try {
                                logger.debug("Service activate: " + service.hashCode() + " : " + serviceName);
                                service.activate();
                                setServiceState(ServiceState.Running);
                            } catch (CouldNotPerformException | NullPointerException ex) {
                                logger.error("Could not start Service[" + serviceName + " " + service.hashCode() + "]!", ex);
                                setServiceState(ServiceState.Failed);
                                logger.info("Try again in " + (DELAY / 1000) + " seconds...");
                            }
                        }
                        waitWithinDelay();
                    }
                } catch (InterruptedException ex) {
                    logger.debug("Catch Service[" + serviceName + "] interruption.");
                    interrupt();
                }

                while (service.isActive()) {
                    setServiceState(ServiceState.Terminating);
                    try {
                        service.deactivate();
                        setServiceState(ServiceState.Finished);
                    } catch (CouldNotPerformException ex) {
                        logger.error("Could not shutdown Service[" + serviceName + "]! Try again in " + (DELAY / 1000) + " seconds...", ex);
                        try {
                            waitWithinDelay();
                        } catch (InterruptedException exx) {
                            logger.debug("Catch Service[" + serviceName + "] interruption during shutdown!");
                            interrupt();
                        }
                    }
                }
            } catch (Throwable tr) {
                logger.error("Fatal watchdog execution error! Release all locks...", tr);
                skipActivation();
            }
        }

        private void waitWithinDelay() throws InterruptedException {
            Thread.sleep(DELAY);
        }
    }

    public Activatable getService() {
        return service;
    }

    private void setServiceState(final ServiceState serviceState) {
        try {
            synchronized (activationLock) {
                if (this.serviceState == serviceState) {
                    return;
                }
                this.serviceState = serviceState;
            }
            logger.debug(this + " is now " + serviceState.name().toLowerCase() + ".");
            serviceStateObserable.notifyObservers(serviceState);
        } catch (MultiException ex) {
            logger.warn("Could not notify state change to all instances!", ex);
            ex.printExceptionStack();
        }
    }

    public ServiceState getServiceState() {
        return serviceState;
    }

    public void addObserver(Observer<ServiceState> observer) {
        serviceStateObserable.addObserver(observer);
    }

    public void removeObserver(Observer<ServiceState> observer) {
        serviceStateObserable.removeObserver(observer);
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + serviceName + "]";
    }
}
