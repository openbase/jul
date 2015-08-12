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
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 * @param <R>
 */
public interface RegistrySandboxInterface<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>, R extends RegistryInterface<KEY, ENTRY, R>> extends RegistryInterface<KEY, ENTRY, R> {

    public void sync(final MAP map);
    
    public void registerConsistencyHandler(final ConsistencyHandler<KEY, ENTRY, MAP, R> consistencyHandler) throws CouldNotPerformException;
    
    public void replaceInternalMap(Map<KEY, ENTRY> map);

}
