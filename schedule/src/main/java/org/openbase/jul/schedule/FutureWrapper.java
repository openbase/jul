package org.openbase.jul.schedule;

import java.util.concurrent.Future;

/**
 * An generic interface definition to passthrough an wrapped future object.
 * @param <T> the type of the wrapped future.
 */
public interface FutureWrapper<T> {

    /**
     * Returns the wrapped future.
     * @return the internal future encapsulated by this wrapper instance.
     */
    Future<T> getInternalFuture();
}
