package org.openbase.jul.pattern.provider;

import org.openbase.jul.exception.NotAvailableException;

/**
 * Generic interface for providing a data object.
 *
 * @param <D> the datatype of the object.
 */
public interface Provider<D> {

    /**
     * Returns the provide data object.
     *
     * @return the value to provide.
     *
     * @throws NotAvailableException is thrown if the provider does not provide something.
     * @throws InterruptedException  is thrown if the thread has been externally interrupted.
     */
    D get() throws NotAvailableException, InterruptedException;
}
