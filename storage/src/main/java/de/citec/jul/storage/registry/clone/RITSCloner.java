package de.citec.jul.storage.registry.clone;

import com.rits.cloning.Cloner;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import java.util.Map;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 * @param <MAP>
 */
public class RITSCloner<KEY, ENTRY extends Identifiable<KEY>, MAP extends Map<KEY, ENTRY>> implements RegistryCloner<KEY, ENTRY, MAP>{

    private final Cloner cloner;

    public RITSCloner() {
        this.cloner = new Cloner();
    }

    @Override
    public MAP deepCloneRegistryMap(MAP map) throws CouldNotPerformException {
        return cloner.deepClone(map);
    }

    @Override
    public Map<KEY, ENTRY> deepCloneMap(Map<KEY, ENTRY> map) throws CouldNotPerformException {
        return cloner.deepClone(map);
    }

    @Override
    public ENTRY deepCloneEntry(ENTRY entry) throws CouldNotPerformException {
        return cloner.deepClone(entry);
    }
}
