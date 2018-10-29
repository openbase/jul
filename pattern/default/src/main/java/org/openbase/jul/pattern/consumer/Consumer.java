package org.openbase.jul.pattern.consumer;

/**
 * Generic interface for consuming a data object.
 *
 * @param <D> the datatype of the object.
 */
public interface Consumer<D> {

    /**
     * Consums the provide data object.
     *
     * @param data the value to consume.
     */
    void consume(final D data);
}
