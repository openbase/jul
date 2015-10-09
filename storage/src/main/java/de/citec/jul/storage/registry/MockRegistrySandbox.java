/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.iface.Identifiable;
import java.util.List;
import java.util.Map;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public class MockRegistrySandbox<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>> implements RegistrySandboxInterface<KEY, ENTRY, MAP, R> {

    @Override
    public ENTRY register(ENTRY entry) throws CouldNotPerformException {
        return entry;
    }

    @Override
    public ENTRY update(ENTRY entry) throws CouldNotPerformException {
        return entry;
    }

    @Override
    public void sync(MAP map) {
    }

    @Override
    public void registerConsistencyHandler(ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException {
    }

    @Override
    public ENTRY remove(ENTRY entry) throws CouldNotPerformException {
        return null;
    }

    @Override
    public ENTRY get(KEY key) throws CouldNotPerformException {
        throw new UnsupportedOperationException("Not supported for mock sandbox.");
    }

    @Override
    public List<ENTRY> getEntries() {
        throw new UnsupportedOperationException("Not supported for mock sandbox.");
    }

    @Override
    public boolean contains(ENTRY entry) throws CouldNotPerformException {
        throw new UnsupportedOperationException("Not supported for mock sandbox.");
    }

    @Override
    public boolean contains(KEY key) throws CouldNotPerformException {
        throw new UnsupportedOperationException("Not supported for mock sandbox.");
    }

    @Override
    public void clear() throws CouldNotPerformException {
    }

    @Override
    public void checkAccess() throws RejectedException {
    }

    @Override
    public boolean isReadOnly() {
        return false;
    }

    @Override
    public void replaceInternalMap(Map<KEY, ENTRY> map) {

    }

    @Override
    public int size() {
        throw new UnsupportedOperationException("Not supported for mock sandbox.");
    }

    public String getName() {
        return getClass().getSimpleName();
    }

    @Override
    public String toString() {
        return getName();
    }
}
