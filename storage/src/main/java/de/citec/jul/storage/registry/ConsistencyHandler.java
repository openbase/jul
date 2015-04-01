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
 */
public interface ConsistencyHandler<KEY, VALUE extends Identifiable<KEY>> {
    
    /**
     * Method for establishing a registry data consistency.
     * Method is called by the registry in case of entry changes. 
     * 
     * @param dataMap the data map of the underlying registry.
     * @param registry the underlying registry.
     * @return should return true if any data modifications are applied. Otherwise false.
     * @throws CouldNotPerformException thrown to handle errors.
     */
    public boolean processData(final Map<KEY, VALUE> dataMap, final Registry<KEY, VALUE> registry) throws CouldNotPerformException;
}
