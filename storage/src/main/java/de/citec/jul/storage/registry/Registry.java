/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.storage.registry.plugin.RegistryPlugin;
import de.citec.jul.exception.InstantiationException;
import de.citec.jul.iface.Identifiable;
import java.util.HashMap;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 */
public class Registry<KEY, ENTRY extends Identifiable<KEY>> extends AbstractRegistry<KEY, ENTRY, HashMap<KEY, ENTRY>, Registry<KEY, ENTRY>, RegistryPlugin<KEY, ENTRY>> {

    public Registry(HashMap<KEY, ENTRY> entryMap) throws InstantiationException {
        super(entryMap);
    }
    
    public Registry() throws InstantiationException {
        super(new HashMap<KEY, ENTRY>());
    }
}
