package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
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
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.InstantiationException;
import org.openbase.jul.exception.MultiException;
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Activatable;
import org.openbase.jul.iface.Shutdownable;
import org.openbase.jul.pattern.Observable;
import org.openbase.jul.pattern.ObservableImpl;
import org.openbase.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
// *
 * @author mpohling
 */
public class WatchDog implements Activatable, Shutdownable {
    
    private final Object EXECUTION_LOCK = new Object();
    private final Object activationLock;
    
    private static final long DELAY = 5000;
    
    public enum ServiceState {
        
        UNKNWON, CONSTRUCTED, INITIALIZING, RUNNING, TERMINATING, FINISHED, FAILED, INTERRUPTED
    };
    
    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final Activatable service;
    private final String serviceName;
    private Minder minder;
    private ServiceState serviceState = ServiceState.UNKNWON;
    
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
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Could not shutdown " + serviceName + "!", ex), logger);
                    }
                }
            });
            
            setServiceState(ServiceState.CONSTRUCTED);
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
        logger.debug("Try to deactivate service: " + serviceName);
        synchronized (EXECUTION_LOCK) {
            logger.debug("Init deactivation of service: " + serviceName);
            if (minder == null) {
                logger.debug("Skip deactivation, Service[" + serviceName + "] not running!");
                return;
            }
            
            logger.debug("Init service interruption...");
            minder.interrupt();
            logger.debug("Wait for service interruption...");
            minder.join();
            minder = null;
            logger.debug("Service interrupted!");
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
            if (serviceState == ServiceState.RUNNING) {
                return;
            }
            
            addObserver(new Observer<ServiceState>() {
                
                @Override
                public void update(final Observable<ServiceState> source, ServiceState data) throws Exception {
                    if (data == ServiceState.RUNNING) {
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
            setServiceState(ServiceState.INITIALIZING);
        }
        
        @Override
        public void run() {
            try {
                try {
                    while (!isInterrupted()) {
                        if (!service.isActive()) {
                            setServiceState(ServiceState.INITIALIZING);
                            try {
                                logger.debug("Service activate: " + service.hashCode() + " : " + serviceName);
                                service.activate();
                                setServiceState(ServiceState.RUNNING);
                            } catch (CouldNotPerformException | NullPointerException ex) {
                                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not start Service[" + serviceName + " " + service.hashCode() + "]!", ex), logger);
                                setServiceState(ServiceState.FAILED);
                                logger.info("Try again in " + (DELAY / 1000) + " seconds...");
                            }
                        }
                        waitWithinDelay();
                    }
                } catch (InterruptedException ex) {
                    /**
                     * An interrupted exception was caught triggered by the deactivate() method of the watchdog.
                     * The minder shutdown will be initiated now.
                     *
                     * !!! Do not recover the interrupted state to grantee a proper shutdown !!!
                     */
                    logger.debug("Minder shutdown initiated of Service[" + serviceName + "]...");
                }
                
                while (service.isActive()) {
                    setServiceState(ServiceState.TERMINATING);
                    try {
                        try {
                            logger.debug("Minder deactivation initiated of Service[" + serviceName + "]...");
                            service.deactivate();
                            setServiceState(ServiceState.FINISHED);
                        } catch (IllegalStateException | CouldNotPerformException ex) {
                            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not shutdown Service[" + serviceName + "]! Try again in " + (DELAY / 1000) + " seconds...", ex), logger);
                            waitWithinDelay();
                        }
                    } catch (InterruptedException ex) {
                        ExceptionPrinter.printHistory(new CouldNotPerformException("Could not terminate Service[" + serviceName + "] because termination was externaly interrupted.", ex), logger, LogLevel.WARN);
                        setServiceState(ServiceState.INTERRUPTED);
                        break;
                    }
                }
            } catch (Throwable tr) {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException("Fatal watchdog execution error! Release all locks...", tr), logger);
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
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not notify state change to all instances!", ex), logger);
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
    public void shutdown() {
        try {
            serviceStateObserable.shutdown();
            deactivate();
        } catch (InterruptedException ex) {
            ExceptionPrinter.printHistory(this + "was interruped during shutdown!", ex, logger);
            Thread.currentThread().interrupt();
        }
    }
    
    @Override
    public String toString() {
        return getClass().getSimpleName() + "[" + serviceName + "]";
    }
}
