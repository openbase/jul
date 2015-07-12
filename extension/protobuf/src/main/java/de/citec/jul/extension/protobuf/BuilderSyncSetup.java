/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage.Builder;
import de.citec.jps.core.JPService;
import de.citec.jps.preset.JPTestMode;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.ExceptionPrinter;
import de.citec.jul.iface.Changeable;
import de.citec.jul.schedule.Timeout;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling
 * @param <MB>
 */
public class BuilderSyncSetup<MB extends Builder<MB>> {

    public static final long LOCK_TIMEOUT = 10000;

    protected final Logger logger = LoggerFactory.getLogger(BuilderSyncSetup.class);

    private final Changeable holder;
    private final MB builder;
    private final ReentrantReadWriteLock.ReadLock readLock;
    private final ReentrantReadWriteLock.WriteLock writeLock;
    private final Timeout readLockTimeout;
    private final Timeout writeLockTimeout;
    private Object readLockConsumer;
    private Object writeLockConsumer;

    public BuilderSyncSetup(final MB builder, final ReentrantReadWriteLock.ReadLock readLock, final ReentrantReadWriteLock.WriteLock writeLock, final Changeable holder) {
        this.builder = builder;
        this.readLock = readLock;
        this.writeLock = writeLock;
        this.holder = holder;
        this.readLockTimeout = new Timeout(LOCK_TIMEOUT) {

            @Override
            public void expired() {
                if (!JPService.getProperty(JPTestMode.class).getValue()) {
                    logger.error("Fatal implementation error!", new TimeoutException("ReadLock of " + builder.getClass().getSimpleName() + " was locked for more than " + LOCK_TIMEOUT / 1000 + " sec! Last access by Consumer[" + readLockConsumer + "]!"));
                    unlockRead("TimeoutHandler");
                }
            }
        };
        this.writeLockTimeout = new Timeout(LOCK_TIMEOUT) {

            @Override
            public void expired() {
                if (!JPService.getProperty(JPTestMode.class).getValue()) {
                    logger.error("Fatal implementation error!", new TimeoutException("WriteLock of " + builder.getClass().getSimpleName() + " was locked for more than " + LOCK_TIMEOUT / 1000 + " sec by Consumer[" + writeLockConsumer + "]!"));
                    unlockWrite();
                }
            }
        };
    }

    /**
     * Returns the internal builder instance. Use builder with care of read and
     * write locks.
     *
     * @return
     */
    public MB getBuilder() {
        return builder;
    }

    public void lockRead(final Object consumer) {
        logger.debug("order lockRead by " + consumer);
        readLock.lock();
        readLockConsumer = consumer;
        readLockTimeout.restart();
        logger.debug("lockRead by " + consumer);
    }

    public boolean tryLockRead(final Object consumer) {
        boolean success = readLock.tryLock();
        if (success) {
            readLockConsumer = consumer;
            readLockTimeout.restart();
        }
        return success;
    }

    public boolean tryLockRead(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {
        boolean success = readLock.tryLock(time, unit);
        if (success) {
            readLockConsumer = consumer;
            readLockTimeout.restart();
        }
        return success;
    }

    public void unlockRead(final Object consumer) {
        logger.debug("order unlockRead by " + consumer);
        if (readLockConsumer == consumer) {
            readLockConsumer = "Unknown";
        }
        readLockTimeout.cancel();
        readLock.unlock();
        logger.debug("unlockRead by " + consumer);
    }

    public void lockWrite(final Object consumer) {
        logger.debug("order lockWrite by " + consumer);
        writeLock.lock();
        writeLockConsumer = consumer;
        writeLockTimeout.start();
        logger.debug("lockWrite by " + consumer);
    }

    public boolean tryLockWrite(final Object consumer) {
        boolean success = writeLock.tryLock();
        if (success) {
            writeLockConsumer = consumer;
            writeLockTimeout.start();
        }
        return success;
    }

    public boolean tryLockWrite(final long time, final TimeUnit unit, final Object consumer) throws InterruptedException {
        boolean success = writeLock.tryLock(time, unit);
        if (success) {
            writeLockConsumer = consumer;
            writeLockTimeout.start();
        }
        return success;
    }

    public void unlockWrite() {
        logger.debug("order write unlock");
        writeLockConsumer = "Unknown";
        writeLockTimeout.cancel();
        writeLock.unlock();
        logger.debug("write unlocked");
        try {
            holder.notifyChange();
        } catch (CouldNotPerformException ex) {
            ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not inform builder holder about data update!", ex));
        }
    }
}
