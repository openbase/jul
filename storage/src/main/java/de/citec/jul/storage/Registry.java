/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage;

import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPReadOnly;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.schedule.SyncObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
 * @param <KEY>
 * @param <VALUE>
 */
public class Registry<KEY, VALUE extends Identifiable<KEY>> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());
    
    private final SyncObject SYNC = new SyncObject(Registry.class);
    private final Map<KEY, VALUE> registry;

    public Registry() {
        this.registry = new HashMap<>();
    }

    public Registry(final Map<KEY, VALUE> registry) {
        this.registry = registry;
    }

    public VALUE register(final VALUE entry) throws CouldNotPerformException {
        logger.info("Register "+entry+"...");
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
        return entry;
    }
    
    public VALUE update(final VALUE entry) throws CouldNotPerformException {
        logger.info("Update "+entry+"...");
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
        return entry;
    }

    public VALUE remove(final VALUE entry) throws CouldNotPerformException {
        logger.info("Remove "+entry+"...");
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
        }
    }

    public VALUE get(final KEY key) throws NotAvailableException {
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

    public boolean contrains(KEY key) {
        return registry.containsKey(key);
    }

    public void clean() {
        synchronized (SYNC) {
            registry.clear();
        }
    }

    public void checkAccess() throws InvalidStateException {
        if (JPService.getProperty(JPReadOnly.class).getValue()) {
            throw new InvalidStateException("ReadOnlyMode is detected!");
        }
    }
}
