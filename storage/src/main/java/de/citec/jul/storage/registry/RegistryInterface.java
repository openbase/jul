/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.iface.Identifiable;
import java.util.List;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <R>
 */
public interface RegistryInterface<KEY, VALUE extends Identifiable<KEY>, R extends RegistryInterface<KEY, VALUE, R>> {

    public VALUE register(final VALUE entry) throws CouldNotPerformException;

    public VALUE update(final VALUE entry) throws CouldNotPerformException;

    public VALUE remove(final VALUE entry) throws CouldNotPerformException;

    public VALUE get(final KEY key) throws CouldNotPerformException;

    public List<VALUE> getEntries();

    public boolean contains(final VALUE entry) throws CouldNotPerformException;

    public boolean contains(final KEY key) throws CouldNotPerformException;

    public void clear() throws CouldNotPerformException ;

    public void checkAccess() throws InvalidStateException;
    
    public int size();

    public boolean isReadOnly();
    
}
