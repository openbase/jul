/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import de.citec.jul.iface.Identifiable;
import java.util.HashMap;

/**
 *
 * @author mpohling
 */
public class Registry<KEY, VALUE extends Identifiable<KEY>> extends AbstractRegistry<KEY, VALUE, HashMap<KEY, VALUE>, Registry<KEY, VALUE>> {

    public Registry() {
        super(new HashMap<KEY, VALUE>());
    }
    
    public Registry(final HashMap<KEY, VALUE> entryMap) {
        super(entryMap);
    }
}
