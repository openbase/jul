package de.citec.jul.extension.protobuf;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.iface.Identifiable;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <VALUE>
 */
public class IdentifiableValueMap<KEY, VALUE extends Identifiable<KEY>> extends HashMap<KEY, VALUE> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    public IdentifiableValueMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IdentifiableValueMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentifiableValueMap() {
    }

    public IdentifiableValueMap(Map<? extends KEY, ? extends VALUE> m) {
        super(m);
    }

    public void put(final VALUE value) throws CouldNotPerformException {
        try {
            put(value.getId(), value);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not put value to list!", ex);
        }
    }

    public VALUE remove(Identifiable<KEY> value) throws CouldNotPerformException {
        return super.remove(value.getId());
    }
}
