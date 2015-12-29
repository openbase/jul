/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.iface.Identifiable;

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
    
    public Integer getDBVersion() throws NotAvailableException;;
}
