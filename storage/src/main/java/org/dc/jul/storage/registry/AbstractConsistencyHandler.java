package org.dc.jul.storage.registry;

import java.util.Map;
import org.dc.jul.iface.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <VALUE>
 * @param <MAP>
 * @param <R>
 */
public abstract class AbstractConsistencyHandler<KEY, VALUE extends Identifiable<KEY>, MAP extends Map<KEY, VALUE>, R extends Registry<KEY, VALUE, R>> implements ConsistencyHandler<KEY, VALUE, MAP, R> {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void reset() {
    }

    @Override
    public boolean shutdown() {
        return true;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName();
    }
}
