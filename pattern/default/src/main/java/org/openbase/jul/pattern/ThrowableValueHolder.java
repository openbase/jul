package org.openbase.jul.pattern;

/**
 * Simple holder interface which holds an throwable instance and provides getter, setter, checks and throw methods to handle the internal field.
 * Holder can be used to pass an throwable instance from inside of a lamda expression to the outer method scope.
 * @param <T>
 */
public interface ThrowableValueHolder<T extends Throwable> extends ValueHolder<T> {

    /**
     * Method thrown the internal throwable if available.
     * @throws T the thrown throwable.
     */
    void throwIfAvailable() throws T;
}