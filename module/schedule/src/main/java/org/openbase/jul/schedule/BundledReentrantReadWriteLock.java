package org.openbase.jul.schedule;

/*
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.ShutdownInProgressException;
import org.openbase.jul.exception.StackTracePrinter;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * This lock can be used to bundle two locks into one.
 * <p>
 * The secondary lock will always be locked first and than the primary (mostly more important) one.
 * The unlock us performed in revise order.
 * <p>
 * Note: When auto lock release is enabled, than the locks are auto released when locked to long by on consumer. Additionally an FatalImplementationErrorException is thrown in this case.
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class BundledReentrantReadWriteLock implements ReadWriteLock {

    public static final long DEFAULT_LOCK_TIMEOUT = TimeUnit.MINUTES.toMillis(1);

    protected final Logger logger = LoggerFactory.getLogger(BundledReentrantReadWriteLock.class);

    private final Object holder;
    private final ReentrantReadWriteLock primaryLock;
    private final ReentrantReadWriteLock secondaryLock;
    private final Timeout readLockTimeout;
    private final Timeout writeLockTimeout;
    private Object readLockConsumer;
    private Object writeLockConsumer;
    private final boolean independentPrimaryReadAccess;

    /**
     * Constructor creates a new bundled lock.
     *
     * @param secondaryLock                a less important lock maybe used for frequently by notification purpose with is locked in advance.
     * @param independentPrimaryReadAccess flag defines if the primary read lock is independent from the secondary lock. If true, read action will not lock the secondary lock.
     * @param holder                       the instance holding the locks.
     */
    public BundledReentrantReadWriteLock(final ReentrantReadWriteLock secondaryLock, final boolean independentPrimaryReadAccess, final Object holder) {
        this(new ReentrantReadWriteLock(), secondaryLock, independentPrimaryReadAccess, holder);
    }


    /**
     * Kind of copy constructor which returns a new clone of the given lock.
     * <p>
     * Note: The timed lock limitation is just a procedure to avoid a blocking system in case external components are buggy.
     * But it should never be used as an implementation strategy because it still can result in strange behaviour.
     * Always release the lock afterwards.
     *
     * @param lock   the instance to clone.
     * @param holder the instance holding the new lock.
     */
    public BundledReentrantReadWriteLock(final BundledReentrantReadWriteLock lock, final boolean independentPrimaryReadAccess, final Object holder) {
        this(lock.primaryLock, lock.secondaryLock, independentPrimaryReadAccess, holder);
    }

    /**
     * Constructor creates a new bundled lock.
     *
     * @param primaryLock                  the more important lock used e.g. for configure or manage an instance.
     * @param secondaryLock                a less important lock maybe used for frequently by notification purpose with is locked in advance.
     * @param independentPrimaryReadAccess flag defines if the primary read lock is independent from the secondary lock. If true, read action will not lock the secondary lock.
     * @param holder                       the instance holding the locks.
     */
    public BundledReentrantReadWriteLock(final ReentrantReadWriteLock primaryLock, final ReentrantReadWriteLock secondaryLock, final boolean independentPrimaryReadAccess, final Object holder) {
        this.secondaryLock = secondaryLock;
        this.primaryLock = primaryLock;
        this.independentPrimaryReadAccess = independentPrimaryReadAccess;

        this.holder = holder;
        this.readLockTimeout = new Timeout(DEFAULT_LOCK_TIMEOUT) {

            @Override
            public void expired() {
                StackTracePrinter.printStackTrace(logger);
                new FatalImplementationErrorException(this, new TimeoutException("ReadLock of " + holder + " was locked for more than " + DEFAULT_LOCK_TIMEOUT / 1000 + " sec! Last access by Consumer[" + readLockConsumer + "]!"));
            }
        };
        this.writeLockTimeout = new Timeout(DEFAULT_LOCK_TIMEOUT) {

            @Override
            public void expired() {
                StackTracePrinter.printStackTrace(logger);
                new FatalImplementationErrorException(this, new TimeoutException("WriteLock of " + holder + " was locked for more than " + DEFAULT_LOCK_TIMEOUT / 1000 + " sec by Consumer[" + writeLockConsumer + "]!"));
            }
        };
    }

    @Override
    public void lockRead() {
        lockRead(holder);
    }

    @Override
    public void lockRead(final Object consumer) {
        //logger.debug("order lockRead by {}", consumer);
        if (!independentPrimaryReadAccess) {
            secondaryLock.readLock().lock();
        }
        primaryLock.readLock().lock();
        readLockConsumer = consumer;
        restartReadLockTimeout();
        //logger.debug("lockRead by {}", consumer);
    }

    @Override
    public void lockReadInterruptibly() throws InterruptedException {
        lockReadInterruptibly(holder);
    }

    @Override
    public void lockReadInterruptibly(final Object consumer) throws InterruptedException {
        //logger.debug("order lockRead by {}", consumer);
        if (!independentPrimaryReadAccess) {
            secondaryLock.readLock().lockInterruptibly();
        }

        try {
            primaryLock.readLock().lockInterruptibly();
        } catch (InterruptedException ex) {
            if (!independentPrimaryReadAccess) {
                // in case th primary lock could not be locked, then we have to release the secondary lock again.
                secondaryLock.readLock().unlock();
            }
            throw ex;
        }

        readLockConsumer = consumer;
        restartReadLockTimeout();
        //logger.debug("lockRead by {}", consumer);
    }

    @Override
    public boolean tryLockRead() {
        return tryLockRead(holder);
    }

    @Override
    public boolean tryLockRead(final Object consumer) {

        if (independentPrimaryReadAccess) {
            final boolean primarySuccess = primaryLock.readLock().tryLock();
            if (primarySuccess) {
                readLockConsumer = consumer;
                restartReadLockTimeout();
            }
            return primarySuccess;
        } else {
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
    }

    @Override
    public boolean tryLockRead(final long time, final TimeUnit unit) throws InterruptedException {
        return tryLockRead(time, unit, holder);
    }

    @Override
    public boolean tryLockRead(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {

        if (independentPrimaryReadAccess) {
            final boolean result = primaryLock.readLock().tryLock(time, unit);
            if (result) {
                readLockConsumer = consumer;
                restartReadLockTimeout();
            }
            return result;
        } else {
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
    }

    @Override
    public void unlockRead() {
        unlockRead(holder);
    }

    @Override
    public void unlockRead(final Object consumer) {
        //logger.debug("order unlockRead by {}", consumer);
        if (readLockConsumer == consumer) {
            readLockConsumer = "Unknown";
        }
        readLockTimeout.cancel();
        primaryLock.readLock().unlock();

        if (!independentPrimaryReadAccess) {
            secondaryLock.readLock().unlock();
        }
        //logger.debug("unlockRead by {}", consumer);
    }

    @Override
    public void lockWrite() {
        lockWrite(holder);
    }

    @Override
    public void lockWrite(final Object consumer) {
        //logger.debug("order lockWrite by {}", consumer);
        secondaryLock.writeLock().lock();
        primaryLock.writeLock().lock();
        writeLockConsumer = consumer;
        restartWriteLockTimeout();
        //logger.debug("lockWrite by {}", consumer);
    }

    @Override
    public void lockWriteInterruptibly() throws InterruptedException {
        lockWriteInterruptibly(holder);
    }

    @Override
    public void lockWriteInterruptibly(final Object consumer) throws InterruptedException {
        //logger.debug("order lockWrite by {}", consumer);
        secondaryLock.writeLock().lockInterruptibly();

        try {
            primaryLock.writeLock().lockInterruptibly();
        } catch (InterruptedException ex) {
            // release secondary lock in case primary could not be locked.
            secondaryLock.writeLock().unlock();
            throw ex;
        }

        writeLockConsumer = consumer;
        restartWriteLockTimeout();
        //logger.debug("lockWrite by {}", consumer);
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
        //logger.debug("order write unlock by {}", consumer);
        writeLockTimeout.cancel();
        primaryLock.writeLock().unlock();
        secondaryLock.writeLock().unlock();
        writeLockConsumer = "Unknown";
        //logger.debug("write unlocked by {}", consumer);
    }

    private void restartReadLockTimeout() {
        try {
            readLockTimeout.restart();
        } catch (ShutdownInProgressException ex) {
            // skip restart
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not setup builder read lock fallback timeout!", ex, logger, LogLevel.WARN);
        }
    }

    private void restartWriteLockTimeout() {
        try {
            writeLockTimeout.restart();
        } catch (ShutdownInProgressException ex) {
            // skip restart
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory("Could not setup builder write lock fallback timeout!", ex, logger, LogLevel.WARN);
        }
    }

    public boolean isPrimaryWriteLockHeldByCurrentThread() {
        return primaryLock.isWriteLockedByCurrentThread();
    }

    public boolean isSecondaryWriteLockHeldByCurrentThread() {
        return secondaryLock.isWriteLockedByCurrentThread();
    }

    public boolean isAnyWriteLockHeldByCurrentThread() {
        return isPrimaryWriteLockHeldByCurrentThread() || isSecondaryWriteLockHeldByCurrentThread();
    }
}
