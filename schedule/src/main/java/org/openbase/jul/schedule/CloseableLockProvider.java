package org.openbase.jul.schedule;

public interface CloseableLockProvider {

    /**
     * This method generates a closable lock wrapper.
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try(final CloseableWriteLockWrapper ignored = getCloseableWriteLock(this)) {
     *         // do important stuff...
     *     }
     * }
     * </pre> In this example the CloseableWriteLockWrapper.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer a responsible instance which consumes the lock.
     * @return a new builder wrapper which already locks the lock.
     */
    CloseableWriteLockWrapper getCloseableWriteLock(final Object consumer);

    /**
     * This method generates a closable lock wrapper.
     *
     * <pre>
     * {@code Usage Example:
     *
     *     try(final CloseableReadLockWrapper ignored = getCloseableReadLock(this)) {
     *         // do important stuff...
     *     }
     * }
     * </pre> In this example the CloseableReadLockWrapper.close method is be called
     * in background after leaving the try brackets.
     *
     * @param consumer a responsible instance which consumes the lock.
     * @return a new builder wrapper which already locks the lock.
     */
    CloseableReadLockWrapper getCloseableReadLock(final Object consumer);
}
