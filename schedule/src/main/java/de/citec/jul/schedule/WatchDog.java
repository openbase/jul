/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.schedule;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.pattern.Observable;
import de.citec.jul.pattern.Observer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Activatable;

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

	private final Observable<ServiceState> serviceStateObserable;

	public WatchDog(final Activatable task, final String serviceName) throws InstantiationException {
		try {

			this.service = task;
			this.serviceName = serviceName;
			this.serviceStateObserable = new Observable<>();
			this.activationLock = new SyncObject(serviceName+"WatchDogLock");

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
				logger.warn("Skip activation, Service[" + serviceName + "] already running!");
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
				logger.warn("Skip deactivation, Service[" + serviceName + "] not running!");
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
				public void update(Observable<ServiceState> source, ServiceState data) throws Exception {
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
                                logger.debug("service activate: "+service.hashCode()+ " : "+serviceName);
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
				}

				while (service.isActive()) {
					setServiceState(ServiceState.Terminating);
					try {
						service.deactivate();
						setServiceState(ServiceState.Finished);
					} catch (CouldNotPerformException | InterruptedException ex) {
						logger.error("Could not shutdown Service[" + serviceName + "]! Try again in " + (DELAY / 1000) + " seconds...", ex);
						try {
							waitWithinDelay();
						} catch (InterruptedException exx) {
							logger.debug("Catch Service[" + serviceName + "] interruption during shutdown!");
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
			logger.warn("Could not notify statechange to all instanzes!", ex);
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
