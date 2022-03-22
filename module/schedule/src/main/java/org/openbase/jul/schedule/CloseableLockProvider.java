package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2022 openbase.org
 * %%
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Lesser Public License for more details.
 * 
 * You should have received a copy of the GNU General Lesser Public
 * License along with this program.  If not, see
 * <http://www.gnu.org/licenses/lgpl-3.0.html>.
 * #L%
 */

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
