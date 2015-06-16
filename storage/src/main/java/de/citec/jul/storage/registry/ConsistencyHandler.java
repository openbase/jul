/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import java.util.Map;

/**
 *
 * @author mpohling
 * ConsistencyHandler can be registered at any registry type and will be informed about data changes via the processData Method. 
 * The handler can be used to establish a registry data consistency. 
 * @param <KEY> the registry key type.
 * @param <VALUE> the registry data value type.
 * @param <MAP>
 * @param <R>
 */
public interface ConsistencyHandler<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends RegistryInterface<KEY, VALUE, R>> {
    
    /**
     * Method for establishing a registry data consistency.
     * Method is called by the registry in case of entry changes. 
     * 
     * @param entryMap the entry map of the underlying registry.
     * @param registry the underlying registry.
     * @throws CouldNotPerformException thrown to handle errors.
     * @throws de.citec.jul.storage.registry.EntryModification in case of entry modification during consistency process.
     */
    public void processData(final KEY id, final VALUE entry, final MAP entryMap, final R registry) throws CouldNotPerformException, EntryModification;
    
    public void reset();
}
