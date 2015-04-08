/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPReadOnly;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.MultiException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.exception.VerificationFailedException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.pattern.Observable;
import de.citec.jul.schedule.SyncObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.locks.Lock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine Threepwood
 * @param <KEY>
 * @param <VALUE>
 */
public class Registry<KEY, VALUE extends Identifiable<KEY>> extends Observable<Map<KEY, VALUE>> {

	protected final Logger logger = LoggerFactory.getLogger(getClass());

	private final SyncObject SYNC = new SyncObject(Registry.class);
	private final Map<KEY, VALUE> registry;
	private final List<ConsistencyHandler<KEY, VALUE>> consistencyHandlerList;

	public Registry() {
		this(new HashMap<KEY, VALUE>());
	}

	public Registry(final Map<KEY, VALUE> registry) {
		this.registry = registry;
		this.consistencyHandlerList = new ArrayList<>();
		this.notifyConsistencyHandler();
		this.notifyObservers();
	}

	public VALUE register(final VALUE entry) throws CouldNotPerformException {
		logger.info("Register " + entry + "...");
		try {
			checkAccess();
			synchronized (SYNC) {
				if (registry.containsKey(entry.getId())) {
					throw new CouldNotPerformException("Could not register " + entry + "! Entry with same id already registered!");
				}
				registry.put(entry.getId(), entry);
			}
		} catch (CouldNotPerformException ex) {
			throw new CouldNotPerformException("Could not register " + entry + "!", ex);
		}
		notifyConsistencyHandler();
		notifyObservers();
		return entry;
	}

	public VALUE update(final VALUE entry) throws CouldNotPerformException {
		logger.info("Update " + entry + "...");
		try {
			checkAccess();
			synchronized (SYNC) {
				if (!registry.containsKey(entry.getId())) {
					throw new InvalidStateException("Entry not registered!");
				}
				// replace
				registry.put(entry.getId(), entry);
			}
		} catch (CouldNotPerformException ex) {
			throw new CouldNotPerformException("Could not update " + entry + "!", ex);
		}
		notifyConsistencyHandler();
		notifyObservers();
		return entry;
	}

	public VALUE remove(final VALUE entry) throws CouldNotPerformException {
		logger.info("Remove " + entry + "...");
		try {
			checkAccess();
			synchronized (SYNC) {
				if (!registry.containsKey(entry.getId())) {
					throw new InvalidStateException("Entry not registered!");
				}
				return registry.remove(entry.getId());
			}
		} catch (CouldNotPerformException ex) {
			throw new CouldNotPerformException("Could not remove " + entry + "!", ex);
		} finally {
			notifyConsistencyHandler();
			notifyObservers();
		}
	}

	public VALUE get(final KEY key) throws CouldNotPerformException {
		verifyID(key);
		synchronized (SYNC) {
			if (!registry.containsKey(key)) {
				TreeMap<KEY, VALUE> sortedMap = new TreeMap<>(registry);
				throw new NotAvailableException("Entry[" + key + "]", "Nearest neighbor is [" + sortedMap.floorKey(key) + "] or [" + sortedMap.ceilingKey(key) + "].");
			}
			return registry.get(key);
		}
	}

	public List<VALUE> getEntries() {
		synchronized (SYNC) {
			return new ArrayList<>(registry.values());
		}
	}

	public boolean contrains(final VALUE entry) throws CouldNotPerformException {
		return contrains(entry.getId());
	}

	public boolean contrains(final KEY key) throws CouldNotPerformException {
		return registry.containsKey(verifyID(key));
	}

	public void clean() {
		synchronized (SYNC) {
			registry.clear();
		}
		notifyObservers();
	}

	protected void replaceInternalMap(final Map<KEY, VALUE> map) {
		synchronized (SYNC) {
			registry.clear();
			registry.putAll(map);
		}
	}

	public void checkAccess() throws InvalidStateException {
		if (JPService.getProperty(JPReadOnly.class).getValue()) {
			throw new InvalidStateException("ReadOnlyMode is detected!");
		}
	}

	private void notifyObservers() {
		try {
			super.notifyObservers(registry);
		} catch (MultiException ex) {
			ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not notify all observer!", ex));
		}
	}

	protected KEY verifyID(VALUE entry) throws VerificationFailedException {
		try {
			return verifyID(entry.getId());
		} catch (CouldNotPerformException ex) {
			throw new VerificationFailedException("Could not verify message!", ex);
		}
	}

	protected KEY verifyID(KEY id) throws VerificationFailedException {
		if (id == null) {
			throw new VerificationFailedException("Invalid id!", new NotAvailableException("id"));
		}
		return id;
	}

	public void registerConsistencyHandler(final ConsistencyHandler<KEY, VALUE> consistencyHandler) {
		consistencyHandlerList.add(consistencyHandler);
	}

	boolean consistencyCheckRunning = false;

	private synchronized void notifyConsistencyHandler() {
		if (consistencyCheckRunning) {
			return;
		}
		consistencyCheckRunning = true;

		int interationCounter = 0;
		boolean valid = false;
		MultiException.ExceptionStack exceptionStack = null;

		synchronized (SYNC) {

			while (!valid && !consistencyHandlerList.isEmpty()) {
				valid = true;
				for (ConsistencyHandler<KEY, VALUE> consistencyHandler : consistencyHandlerList) {
					try {
						valid &= !consistencyHandler.processData(registry, this);
					} catch (Exception ex) {
						exceptionStack = MultiException.push(consistencyHandler, new VerificationFailedException("Could not verify registry data consistency!", ex), exceptionStack);
						valid = false;
					}
				}

				interationCounter++;

				// handle handler interfereience
				if (!valid && interationCounter > consistencyHandlerList.size() * 2) {
					try {
						MultiException.checkAndThrow("To many errors occoured during processing!", exceptionStack);
						throw new InvalidStateException("ConsistencyHandler interference detected!");
					} catch (CouldNotPerformException ex) {
						ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Consistency process aborted!", ex));
					}
					logger.warn("Registry data not consistent!");
					break;
				}
			}
		}
		consistencyCheckRunning = false;
	}

	@Override
	public void shutdown() {
		clean();
		super.shutdown();
	}
}
