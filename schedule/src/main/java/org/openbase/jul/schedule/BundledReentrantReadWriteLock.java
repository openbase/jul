package org.openbase.jul.schedule;

/*
 * #%L
 * JUL Schedule
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import org.openbase.jps.core.JPService;
import org.openbase.jps.preset.JPTestMode;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This lock can be used to bundle to locks into one.
 *
 * The secondary lock will always be locked first and than the primary (mostly more important) one.
 * The unlock us performed in revise order.
 *
 * Note: When auto lock release is enabled, than the locks are auto released when locked to long by on consumer. Additionally an FatalImplementationErrorException is thrown in this case.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class BundledReentrantReadWriteLock implements ReadWriteLock {

    public static final long LOCK_TIMEOUT = 10000;

    protected final Logger logger = LoggerFactory.getLogger(BundledReentrantReadWriteLock.class);

    private final Object holder;
    private final ReentrantReadWriteLock primaryLock;
    private final ReentrantReadWriteLock secondaryLock;
    private final Timeout readLockTimeout;
    private final Timeout writeLockTimeout;
    private Object readLockConsumer;
    private Object writeLockConsumer;
    final boolean autoLockReleaseOnLongtermBlock;

    /**
     * Constructor creates a new bundled lock.
     * @param secondaryLock a less important lock maybe used for frequently by notification purpose with is locked in advance.
     *
     * @param autoLockReleaseOnLongtermBlock if a consumer blocks the lock it longterm and this flag is true, than the lock will auto released after 10 seconds.
     * @param holder the instance holding the locks.
     */
    public BundledReentrantReadWriteLock(final ReentrantReadWriteLock secondaryLock, final boolean autoLockReleaseOnLongtermBlock, final Object holder) {
        this(new ReentrantReadWriteLock(), secondaryLock, autoLockReleaseOnLongtermBlock, holder);
    }

    /**
     * Constructor creates a new bundled lock.
     * @param primaryLock the more important lock used e.g. for configure or manage an instance.
     * @param secondaryLock a less important lock maybe used for frequently by notification purpose with is locked in advance.
     *
     * @param autoLockReleaseOnLongtermBlock if a consumer blocks the lock it longterm and this flag is true, than the lock will auto released after 10 seconds.
     * @param holder the instance holding the locks.
     */
    public BundledReentrantReadWriteLock(final ReentrantReadWriteLock primaryLock, final ReentrantReadWriteLock secondaryLock, final boolean autoLockReleaseOnLongtermBlock, final Object holder) {
        this.secondaryLock = secondaryLock;
        this.primaryLock = primaryLock;
        this.autoLockReleaseOnLongtermBlock = autoLockReleaseOnLongtermBlock;

        this.holder = holder;
        this.readLockTimeout = new Timeout(LOCK_TIMEOUT) {

            @Override
            public void expired() {
                if (JPService.getValue(JPTestMode.class, false)) {
                    return;
                }
                ExceptionPrinter.printHistory(new FatalImplementationErrorException(this, new TimeoutException("ReadLock of " + holder + " was locked for more than " + LOCK_TIMEOUT / 1000 + " sec! Last access by Consumer[" + readLockConsumer + "]!")), logger);
                if (autoLockReleaseOnLongtermBlock) {
                    unlockRead("TimeoutHandler");
                }
            }
        };
        this.writeLockTimeout = new Timeout(LOCK_TIMEOUT) {

            @Override
            public void expired() {
                if (JPService.getValue(JPTestMode.class, false)) {
                    return;
                }
                ExceptionPrinter.printHistory(new FatalImplementationErrorException(this, new TimeoutException("WriteLock of " + holder + " was locked for more than " + LOCK_TIMEOUT / 1000 + " sec by Consumer[" + writeLockConsumer + "]!")), logger);
                if (autoLockReleaseOnLongtermBlock) {
                    unlockWrite("TimeoutHandler");
                }
            }
        };
    }

    @Override
    public void lockRead() {
        lockRead(holder);
    }

    @Override
    public void lockRead(final Object consumer) {
        logger.debug("order lockRead by {}", consumer);
        secondaryLock.readLock().lock();
        primaryLock.readLock().lock();
        readLockConsumer = consumer;
        restartReadLockTimeout();
        logger.debug("lockRead by {}", consumer);
    }

    @Override
    public boolean tryLockRead() {
        return tryLockRead(holder);
    }

    @Override
    public boolean tryLockRead(final Object consumer) {
        final boolean secondarySuccess = secondaryLock.readLock().tryLock();

        if (secondarySuccess) {
            final boolean primarySuccess = primaryLock.readLock().tryLock();
            if (primarySuccess) {
                readLockConsumer = consumer;
                restartReadLockTimeout();
            } else {
                secondaryLock.readLock().unlock();
            }
            return primarySuccess && secondarySuccess;
        }
        return false;
    }

    @Override
    public boolean tryLockRead(final long time, final TimeUnit unit) throws InterruptedException {
        return tryLockRead(time, unit, holder);
    }

    @Override
    public boolean tryLockRead(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {
        final boolean secondarySuccess = secondaryLock.readLock().tryLock(time, unit);

        if (secondarySuccess) {
            final boolean primarySuccess = primaryLock.readLock().tryLock(time, unit);
            if (primarySuccess) {
                readLockConsumer = consumer;
                restartReadLockTimeout();
            } else {
                secondaryLock.readLock().unlock();
            }
            return primarySuccess && secondarySuccess;
        }
        return false;
    }

    @Override
    public void unlockRead() {
        unlockRead(holder);
    }

    @Override
    public void unlockRead(final Object consumer) {
        logger.debug("order unlockRead by {}", consumer);
        if (readLockConsumer == consumer) {
            readLockConsumer = "Unknown";
        }
        readLockTimeout.cancel();
        primaryLock.readLock().unlock();
        secondaryLock.readLock().unlock();
        logger.debug("unlockRead by {}", consumer);
    }

    @Override
    public void lockWrite() {
        lockWrite(holder);
    }

    @Override
    public void lockWrite(final Object consumer) {
        logger.debug("order lockWrite by {}", consumer);
        secondaryLock.writeLock().lock();
        primaryLock.writeLock().lock();
        writeLockConsumer = consumer;
        restartWriteLockTimeout();
        logger.debug("lockWrite by {}", consumer);
    }

    @Override
    public boolean tryLockWrite(final Object consumer) {
        final boolean secondarySuccess = secondaryLock.writeLock().tryLock();

        if (secondarySuccess) {
            final boolean primarySuccess = primaryLock.writeLock().tryLock();
            if (primarySuccess) {
                readLockConsumer = consumer;
                restartReadLockTimeout();
            } else {
                secondaryLock.writeLock().unlock();
            }
            return primarySuccess && secondarySuccess;
        }
        return false;
    }

    @Override
    public boolean tryLockWrite(final long time, final TimeUnit unit) throws InterruptedException {
        return tryLockWrite(time, unit, holder);
    }

    @Override
    public boolean tryLockWrite(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {
        final boolean secondarySuccess = secondaryLock.writeLock().tryLock(time, unit);

        if (secondarySuccess) {
            final boolean primarySuccess = primaryLock.writeLock().tryLock(time, unit);
            if (primarySuccess) {
                readLockConsumer = consumer;
                restartReadLockTimeout();
            } else {
                secondaryLock.writeLock().unlock();
            }
            return primarySuccess && secondarySuccess;
        }
        return false;
    }

    @Override
    public void unlockWrite() {
        unlockWrite(holder);
    }

    @Override
    public void unlockWrite(final Object consumer) {
        logger.debug("order write unlock by {}", consumer);
        writeLockTimeout.cancel();
        primaryLock.writeLock().unlock();
        secondaryLock.writeLock().unlock();
        writeLockConsumer = "Unknown";
        logger.debug("write unlocked by {}", consumer);
    }

    private void restartReadLockTimeout() {
        try {
            readLockTimeout.restart();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not setup builder read lock fallback timeout!", ex, logger, LogLevel.WARN);
        }
    }

    private void restartWriteLockTimeout() {
        try {
            writeLockTimeout.restart();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not setup builder write lock fallback timeout!", ex, logger, LogLevel.WARN);
        }
    }

    /**
     * Returns a new clone of this lock with @{code autoLockReleaseOnLongtermBlock} enabled (see constructor).
     *
     * Note: The timed lock limitation is just a procedure to avoid a blocking system in case external components are buggy.
     * But it should never be used as an implementation strategy because it still can result in strange behaviour.
     * Always release the lock afterwards.
     *
     * @param holder the instance holding the new lock.
     * @return a new lock instance sharing the same internal locks but with limited access time.
     */
    public BundledReentrantReadWriteLock getTimelimitedClone(final Object holder) {
        return new BundledReentrantReadWriteLock(primaryLock, secondaryLock, true, holder);
    }
}
