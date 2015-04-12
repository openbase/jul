/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <VALUE>
 * @param <R>
 */
public interface FileSynchronizedRegistryInterface<KEY, VALUE extends Identifiable<KEY>, R extends FileSynchronizedRegistryInterface<KEY, VALUE, R>> extends RegistryInterface<KEY, VALUE, R> {

    public void loadRegistry() throws CouldNotPerformException;

    public void saveRegistry() throws CouldNotPerformException;
}
