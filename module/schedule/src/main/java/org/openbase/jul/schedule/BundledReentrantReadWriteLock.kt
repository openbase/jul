package org.openbase.jul.schedule

import org.openbase.jps.core.JPService
import org.openbase.jul.exception.CouldNotPerformException
import org.openbase.jul.exception.FatalImplementationErrorException
import org.openbase.jul.exception.ShutdownInProgressException
import org.openbase.jul.exception.StackTracePrinter
import org.openbase.jul.exception.StackTracePrinter.detectDeadLocksAndPrintStackTraces
import org.openbase.jul.exception.StackTracePrinter.printAllStackTraces
import org.openbase.jul.exception.printer.ExceptionPrinter
import org.openbase.jul.exception.printer.LogLevel
import org.openbase.jul.schedule.BundledReentrantReadWriteLock
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import java.util.concurrent.locks.ReentrantReadWriteLock

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
 */ /**
 * This lock can be used to bundle two locks into one.
 *
 *
 * The secondary lock will always be locked first and than the primary (mostly more important) one.
 * The unlock us performed in revise order.
 *
 *
 * Note: When auto lock release is enabled, than the locks are auto released when locked to long by on consumer. Additionally an FatalImplementationErrorException is thrown in this case.
 *
 * @author [Divine Threepwood](mailto:divine@openbase.org)
 */
class BundledReentrantReadWriteLock(
    private val primaryLock: ReentrantReadWriteLock,
    private val secondaryLock: ReentrantReadWriteLock,
    private val independentPrimaryReadAccess: Boolean,
    private val holder: Any,
) : ReadWriteLock {
    protected val logger = LoggerFactory.getLogger(BundledReentrantReadWriteLock::class.java)
    private val readLockTimeout: Timeout
    private val writeLockTimeout: Timeout
    private var readLockConsumer: Any? = null
        set(value) {
            field = value
            if (JPService.debugMode()) {
                readStacktrace = Thread.currentThread().stackTrace
            }
        }
    private var writeLockConsumer: Any? = null
        set(value) {
            field = value
            if (JPService.debugMode()) {
                writeStacktrace = Thread.currentThread().stackTrace
            }
        }

    private var readStacktrace: Array<StackTraceElement>? = null
    private var writeStacktrace: Array<StackTraceElement>? = null

    /**
     * Constructor creates a new bundled lock.
     *
     * @param secondaryLock                a less important lock maybe used for frequently by notification purpose with is locked in advance.
     * @param independentPrimaryReadAccess flag defines if the primary read lock is independent from the secondary lock. If true, read action will not lock the secondary lock.
     * @param holder                       the instance holding the locks.
     */
    constructor(secondaryLock: ReentrantReadWriteLock, independentPrimaryReadAccess: Boolean, holder: Any) : this(
        ReentrantReadWriteLock(),
        secondaryLock,
        independentPrimaryReadAccess,
        holder
    )

    /**
     * Kind of copy constructor which returns a new clone of the given lock.
     *
     *
     * Note: The timed lock limitation is just a procedure to avoid a blocking system in case external components are buggy.
     * But it should never be used as an implementation strategy because it still can result in strange behaviour.
     * Always release the lock afterwards.
     *
     * @param lock   the instance to clone.
     * @param holder the instance holding the new lock.
     */
    constructor(
        lock: BundledReentrantReadWriteLock,
        independentPrimaryReadAccess: Boolean,
        holder: Any,
    ) : this(lock.primaryLock, lock.secondaryLock, independentPrimaryReadAccess, holder)

    /**
     * Constructor creates a new bundled lock.
     *
     * @param primaryLock                  the more important lock used e.g. for configure or manage an instance.
     * @param secondaryLock                a less important lock maybe used for frequently by notification purpose with is locked in advance.
     * @param independentPrimaryReadAccess flag defines if the primary read lock is independent from the secondary lock. If true, read action will not lock the secondary lock.
     * @param holder                       the instance holding the locks.
     */
    init {
        readLockTimeout = object : Timeout(DEFAULT_LOCK_TIMEOUT) {
            override fun expired() {
                readStacktrace?.also { StackTracePrinter.printStackTrace(it, logger, LogLevel.ERROR) }
                detectDeadLocksAndPrintStackTraces(logger)
                StackTracePrinter.printStackTrace(readStacktrace, logger, LogLevel.ERROR)
                FatalImplementationErrorException(
                    this,
                    TimeoutException("ReadLock of " + holder + " was locked for more than " + DEFAULT_LOCK_TIMEOUT / 1000 + " sec! Last access by Consumer[" + readLockConsumer + "]!")
                )
            }
        }
        writeLockTimeout = object : Timeout(DEFAULT_LOCK_TIMEOUT) {
            override fun expired() {
                writeStacktrace?.also { StackTracePrinter.printStackTrace(it, logger, LogLevel.ERROR) }
                detectDeadLocksAndPrintStackTraces(logger)
                FatalImplementationErrorException(
                    this,
                    TimeoutException("WriteLock of " + holder + " was locked for more than " + DEFAULT_LOCK_TIMEOUT / 1000 + " sec by Consumer[" + writeLockConsumer + "]!")
                )
            }
        }
    }

    override fun lockRead() {
        lockRead(holder)
    }

    override fun lockRead(consumer: Any) {
        //logger.debug("order lockRead by {}", consumer);
        if (!independentPrimaryReadAccess) {
            secondaryLock.readLock().lock()
        }
        primaryLock.readLock().lock()
        readLockConsumer = consumer
        if (JPService.debugMode()) {
            readStacktrace = Thread.currentThread().stackTrace
        }
        restartReadLockTimeout()
        //logger.debug("lockRead by {}", consumer);
    }

    @Throws(InterruptedException::class)
    override fun lockReadInterruptibly() = lockReadInterruptibly(holder)

    @Throws(InterruptedException::class)
    override fun lockReadInterruptibly(consumer: Any) {
        //logger.debug("order lockRead by {}", consumer);
        if (!independentPrimaryReadAccess) {
            secondaryLock.readLock().lockInterruptibly()
        }
        try {
            primaryLock.readLock().lockInterruptibly()
        } catch (ex: InterruptedException) {
            if (!independentPrimaryReadAccess) {
                // in case th primary lock could not be locked, then we have to release the secondary lock again.
                secondaryLock.readLock().unlock()
            }
            throw ex
        }
        readLockConsumer = consumer
        restartReadLockTimeout()
        //logger.debug("lockRead by {}", consumer);
    }

    override fun tryLockRead(): Boolean = tryLockRead(holder)

    override fun tryLockRead(consumer: Any): Boolean =
        if (independentPrimaryReadAccess) {
            val primarySuccess = primaryLock.readLock().tryLock()
            if (primarySuccess) {
                readLockConsumer = consumer
                restartReadLockTimeout()
            }
            primarySuccess
        } else {
            val secondarySuccess = secondaryLock.readLock().tryLock()
            if (secondarySuccess) {
                val primarySuccess = primaryLock.readLock().tryLock()
                if (primarySuccess) {
                    readLockConsumer = consumer
                    restartReadLockTimeout()
                } else {
                    secondaryLock.readLock().unlock()
                }
                primarySuccess
            } else {
                false
            }
        }

    @Throws(InterruptedException::class)
    override fun tryLockRead(time: Long, unit: TimeUnit): Boolean = tryLockRead(time, unit, holder)

    @Throws(InterruptedException::class)
    override fun tryLockRead(time: Long, unit: TimeUnit, consumer: Any): Boolean =
        if (independentPrimaryReadAccess) {
            val result = primaryLock.readLock().tryLock(time, unit)
            if (result) {
                readLockConsumer = consumer
                restartReadLockTimeout()
            }
            result
        } else {
            val secondarySuccess = secondaryLock.readLock().tryLock(time, unit)
            if (secondarySuccess) {
                val primarySuccess = primaryLock.readLock().tryLock(time, unit)
                if (primarySuccess) {
                    readLockConsumer = consumer
                    restartReadLockTimeout()
                } else {
                    secondaryLock.readLock().unlock()
                }
                primarySuccess
            } else {
                false
            }
        }

    override fun unlockRead() = unlockRead(holder)

    override fun unlockRead(consumer: Any) {
        //logger.debug("order unlockRead by {}", consumer);
        if (readLockConsumer === consumer) {
            readLockConsumer = "Unknown"
        }
        readLockTimeout.cancel()
        primaryLock.readLock().unlock()
        if (!independentPrimaryReadAccess) {
            secondaryLock.readLock().unlock()
        }
        //logger.debug("unlockRead by {}", consumer);
    }

    override fun lockWrite() = lockWrite(holder)

    override fun lockWrite(consumer: Any) {
        //logger.debug("order lockWrite by {}", consumer);
        secondaryLock.writeLock().lock()
        primaryLock.writeLock().lock()
        writeLockConsumer = consumer
        restartWriteLockTimeout()
        //logger.debug("lockWrite by {}", consumer);
    }

    @Throws(InterruptedException::class)
    override fun lockWriteInterruptibly() = lockWriteInterruptibly(holder)

    @Throws(InterruptedException::class)
    override fun lockWriteInterruptibly(consumer: Any) {
        //logger.debug("order lockWrite by {}", consumer);
        secondaryLock.writeLock().lockInterruptibly()
        try {
            primaryLock.writeLock().lockInterruptibly()
        } catch (ex: InterruptedException) {
            // release secondary lock in case primary could not be locked.
            secondaryLock.writeLock().unlock()
            throw ex
        }
        writeLockConsumer = consumer
        restartWriteLockTimeout()
        //logger.debug("lockWrite by {}", consumer);
    }

    override fun tryLockWrite(consumer: Any): Boolean {
        val secondarySuccess = secondaryLock.writeLock().tryLock()
        if (secondarySuccess) {
            val primarySuccess = primaryLock.writeLock().tryLock()
            if (primarySuccess) {
                readLockConsumer = consumer
                restartReadLockTimeout()
            } else {
                secondaryLock.writeLock().unlock()
            }
            return primarySuccess
        }
        return false
    }

    @Throws(InterruptedException::class)
    override fun tryLockWrite(time: Long, unit: TimeUnit): Boolean = tryLockWrite(time, unit, holder)

    @Throws(InterruptedException::class)
    override fun tryLockWrite(time: Long, unit: TimeUnit, consumer: Any): Boolean {
        val secondarySuccess = secondaryLock.writeLock().tryLock(time, unit)
        if (secondarySuccess) {
            val primarySuccess = primaryLock.writeLock().tryLock(time, unit)
            if (primarySuccess) {
                readLockConsumer = consumer
                restartReadLockTimeout()
            } else {
                secondaryLock.writeLock().unlock()
            }
            return primarySuccess
        }
        return false
    }

    override fun unlockWrite() {
        unlockWrite(holder)
    }

    override fun unlockWrite(consumer: Any) {
        //logger.debug("order write unlock by {}", consumer);
        writeLockTimeout.cancel()
        primaryLock.writeLock().unlock()
        secondaryLock.writeLock().unlock()
        writeLockConsumer = "Unknown"
        //logger.debug("write unlocked by {}", consumer);
    }

    private fun restartReadLockTimeout() {
        try {
            readLockTimeout.restart()
        } catch (ex: ShutdownInProgressException) {
            // skip restart
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory(
                "Could not setup builder read lock fallback timeout!",
                ex,
                logger,
                LogLevel.WARN
            )
        }
    }

    private fun restartWriteLockTimeout() {
        try {
            writeLockTimeout.restart()
        } catch (ex: ShutdownInProgressException) {
            // skip restart
        } catch (ex: CouldNotPerformException) {
            ExceptionPrinter.printHistory(
                "Could not setup builder write lock fallback timeout!",
                ex,
                logger,
                LogLevel.WARN
            )
        }
    }

    val isPrimaryWriteLockHeldByCurrentThread: Boolean
        get() = primaryLock.isWriteLockedByCurrentThread
    val isSecondaryWriteLockHeldByCurrentThread: Boolean
        get() = secondaryLock.isWriteLockedByCurrentThread
    val isAnyWriteLockHeldByCurrentThread: Boolean
        get() = isPrimaryWriteLockHeldByCurrentThread || isSecondaryWriteLockHeldByCurrentThread

    companion object {
        val DEFAULT_LOCK_TIMEOUT = TimeUnit.MINUTES.toMillis(1)
    }
}
