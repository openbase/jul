package org.openbase.jul.pattern;

import org.openbase.jul.exception.NotAvailableException;

/**
 * Simple holder interface which holds an value instance and provides getter, setter and checks to handle the internal value field.
 * Holder can be used to pass an value instance from inside of a lamda expression to the outer method scope.
 *
 * @param <V> the type of the internal value.
 */
public interface ValueHolder<V> {

    /**
     * Method returns the value if available.
     *
     * @return the internal value.
     *
     * @throws NotAvailableException is thrown if the value was never set.
     */
    V getValue() throws NotAvailableException;

    /**
     * Method stores the given {@code value} within the holder.
     * @param value the value to store.
     */
    public void setValue(V value);

    /**
     * Method return is the value was ever set.
     * @return true if the value is available.
     */
    public boolean isValueAvailable();
}
