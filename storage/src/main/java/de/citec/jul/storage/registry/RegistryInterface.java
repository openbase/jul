/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.iface.Identifiable;
import java.util.List;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 * @param <R>
 */
public interface RegistryInterface<KEY, ENTRY extends Identifiable<KEY>, R extends RegistryInterface<KEY, ENTRY, R>> {

    public String getName();

    public ENTRY register(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY update(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY remove(final ENTRY entry) throws CouldNotPerformException;

    public ENTRY get(final KEY key) throws CouldNotPerformException;

    public List<ENTRY> getEntries() throws CouldNotPerformException;

    public boolean contains(final ENTRY entry) throws CouldNotPerformException;

    public boolean contains(final KEY key) throws CouldNotPerformException;

    public void clear() throws CouldNotPerformException;

    public void checkAccess() throws RejectedException;

    public int size();

    public boolean isReadOnly();

    public boolean isConsistent();

}
