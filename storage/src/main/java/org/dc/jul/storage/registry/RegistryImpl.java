/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry;

import org.dc.jul.storage.registry.plugin.RegistryPlugin;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.iface.Identifiable;
import java.util.HashMap;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <ENTRY>
 */
public class RegistryImpl<KEY, ENTRY extends Identifiable<KEY>> extends AbstractRegistry<KEY, ENTRY, HashMap<KEY, ENTRY>, RegistryImpl<KEY, ENTRY>, RegistryPlugin<KEY, ENTRY>> {

    public RegistryImpl(HashMap<KEY, ENTRY> entryMap) throws InstantiationException {
        super(entryMap);
    }
    
    public RegistryImpl() throws InstantiationException {
        super(new HashMap<KEY, ENTRY>());
    }
}
