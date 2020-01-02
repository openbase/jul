package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2020 openbase.org
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

/**
 *
 * Can be used to make the @{Closeable} interface available to any @{ReadWriteLock} instance.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class CloseableReadLockWrapper implements AutoCloseable {

    private final ReadWriteLock lock;

    public CloseableReadLockWrapper(final ReadWriteLock lock) {
        this.lock = lock;
        this.lock.lockRead();
    }

    @Override
    public void close() {
        lock.unlockRead();
    }
}
