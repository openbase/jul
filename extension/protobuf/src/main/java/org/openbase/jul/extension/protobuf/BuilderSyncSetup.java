package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
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

import com.google.protobuf.AbstractMessage.Builder;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import org.openbase.jps.core.JPService;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.ExceptionProcessor;
import org.openbase.jul.exception.FatalImplementationErrorException;
import org.openbase.jul.exception.ShutdownInProgressException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.pattern.ChangeListener;
import org.openbase.jul.schedule.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @param <MB>
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class BuilderSyncSetup<MB extends Builder<MB>> {

    public static final long LOCK_TIMEOUT = 10000;

    protected final Logger logger = LoggerFactory.getLogger(BuilderSyncSetup.class);

    private final ChangeListener holder;
    private final MB builder;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final Timeout readLockTimeout;
    private final Timeout writeLockTimeout;
    private Object readLockConsumer;
    private Object writeLockConsumer;

    public BuilderSyncSetup(final MB builder, final ReentrantReadWriteLock.ReadLock readLock, final ReentrantReadWriteLock.WriteLock writeLock, final ChangeListener holder) {
        this.builder = builder;
        this.readLock = readLock;
        this.writeLock = writeLock;
        this.holder = holder;
        this.readLockTimeout = new Timeout(LOCK_TIMEOUT) {

            @Override
            public void expired() {
                ExceptionPrinter.printHistory(new FatalImplementationErrorException(this, new TimeoutException("ReadLock of " + builder.buildPartial().getClass().getSimpleName() + " was locked for more than " + LOCK_TIMEOUT / 1000 + " sec! Last access by Consumer[" + readLockConsumer + "]!")), logger);

                // in test or debug mode we want to skip the unlock since its only a fallback strategy during 24/7 operation.
                if (JPService.testMode() || JPService.debugMode()) {
                    return;
                }

                // todo: the following code can not work since its not the same thread which unlocks the lock and therefore an illegal monitor state exception is always throws without any effect.
//                try {
//                    unlockRead("TimeoutHandler");
//                } catch (IllegalMonitorStateException ex) {
                    logger.warn("ReadLock recovery of " + builder.buildPartial().getClass().getSimpleName() + " was not successful!");
//                }
            }
        };
        this.writeLockTimeout = new Timeout(LOCK_TIMEOUT) {

            @Override
            public void expired() {
                new FatalImplementationErrorException(this, new TimeoutException("WriteLock of " + builder.buildPartial().getClass().getSimpleName() + " was locked for more than " + LOCK_TIMEOUT / 1000 + " sec by Consumer[" + writeLockConsumer + "]!"));

                // in test or debug mode we want to skip the unlock since its only a fallback strategy during 24/7 operation.
                if (JPService.testMode() || JPService.debugMode()) {
                    return;
                }

                try {
                    unlockWrite();
                } catch (IllegalMonitorStateException ex) {
                    logger.warn("WriteLock recovery of " + builder.buildPartial().getClass().getSimpleName() + " was not successful!");
                }
            }
        };
    }

    /**
     * Returns the internal builder instance.
     * Use builder with care of read and write locks.
     *
     * @return
     */
    public MB getBuilder() {
        return builder;
    }

    public void lockRead(final Object consumer) {
        //logger.debug("order lockRead by {}", consumer);
        readLock.lock();
        readLockConsumer = consumer;
        restartReadLockTimeout();
        //logger.debug("lockRead by {}", consumer);
    }

    public boolean tryLockRead(final Object consumer) {
        boolean success = readLock.tryLock();
        if (success) {
            readLockConsumer = consumer;
            restartReadLockTimeout();
        }
        return success;
    }

    public boolean tryLockRead(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {
        boolean success = readLock.tryLock(time, unit);
        if (success) {
            readLockConsumer = consumer;
            restartReadLockTimeout();
        }
        return success;
    }

    public void unlockRead(final Object consumer) {
        //logger.debug("order unlockRead by {}", consumer);
        if (readLockConsumer == consumer) {
            readLockConsumer = "Unknown";
        }
        readLockTimeout.cancel();
        readLock.unlock();
        //logger.debug("unlockRead by {}", consumer);
    }

    public void lockWrite(final Object consumer) {
        //logger.debug("order lockWrite by {}", consumer);
        writeLock.lock();
        writeLockConsumer = consumer;
        restartWriteLockTimeout();
        //logger.debug("lockWrite by {}", consumer);
    }

    public boolean tryLockWrite(final Object consumer) {
        boolean success = writeLock.tryLock();
        if (success) {
            writeLockConsumer = consumer;
            restartWriteLockTimeout();
        }
        return success;
    }

    public boolean tryLockWrite(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {
        boolean success = writeLock.tryLock(time, unit);
        if (success) {
            writeLockConsumer = consumer;
            restartWriteLockTimeout();
        }
        return success;
    }

    /**
     * Method unlocks the write lock.
     */
    public void unlockWrite() {
        unlockWrite(true);
    }

    /**
     * Method unlocks the write lock and notifies the change to the internal data holder.
     * In case the thread is externally interrupted, no InterruptedException is thrown but instead the interrupted flag is set for the corresponding thread.
     * Please use the service method Thread.currentThread().isInterrupted() to get informed about any external interruption.
     *
     * @param notifyChange
     */
    public void unlockWrite(boolean notifyChange) {
        //logger.debug("order write unlock");
        writeLockTimeout.cancel();
        writeLock.unlock();
        writeLockConsumer = "Unknown";
        //logger.debug("write unlocked");
        if (notifyChange) {
            try {
                try {
                    holder.notifyChange();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            } catch (CouldNotPerformException ex) {
                // only print error if the exception was not caused by a system shutdown.
                if (!ExceptionProcessor.isCausedBySystemShutdown(ex)) {
                    ExceptionPrinter.printHistory(new CouldNotPerformException("Could not inform builder holder about data update!", ex), logger, LogLevel.ERROR);
                }
            }
        }
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
}
