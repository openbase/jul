package org.openbase.jul.schedule;

/*-
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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

import java.util.concurrent.TimeUnit;

public interface ReadWriteLock {
    void lockRead();

    void lockRead(Object consumer);

    void lockReadInterruptibly() throws InterruptedException;

    void lockReadInterruptibly(Object consumer) throws InterruptedException;

    boolean tryLockRead();

    boolean tryLockRead(Object consumer);

    boolean tryLockRead(long time, TimeUnit unit) throws InterruptedException;

    boolean tryLockRead(long time, TimeUnit unit, Object consumer) throws InterruptedException;

    void unlockRead();

    void unlockRead(Object consumer);

    void lockWrite();

    void lockWrite(Object consumer);

    void lockWriteInterruptibly() throws InterruptedException;

    void lockWriteInterruptibly(Object consumer) throws InterruptedException;

    boolean tryLockWrite(Object consumer);

    boolean tryLockWrite(long time, TimeUnit unit) throws InterruptedException;

    boolean tryLockWrite(long time, TimeUnit unit, Object consumer) throws InterruptedException;

    void unlockWrite();

    /**
     * Method unlocks the write lock of the primary and secondary lock.
     * @param consumer the responsible instance which is performing the unlock.
     */
    void unlockWrite(Object consumer);
}
