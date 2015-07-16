/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.collection;

import de.citec.jul.iface.Identifiable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class IdentifiableValueMap<ID extends Identifiable> extends HashMap<ID, Identifiable<ID>> {

    public IdentifiableValueMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IdentifiableValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentifiableValueMap() {
    }

    public IdentifiableValueMap(Map<? extends ID, ? extends Identifiable<ID>> m) {
        super(m);
    }
    
    
    
    
}
