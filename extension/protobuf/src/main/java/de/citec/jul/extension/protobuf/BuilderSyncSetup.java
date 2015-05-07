/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 *
 * @author mpohling
 * @param <MB>
 */
public class BuilderSyncSetup<MB extends GeneratedMessage.Builder> implements ReadWriteLock {

        private final MB builder;
        private final ReentrantReadWriteLock.ReadLock readLock;
        private final ReentrantReadWriteLock.WriteLock writeLock;

        public BuilderSyncSetup(MB builder, ReentrantReadWriteLock.ReadLock readLock, ReentrantReadWriteLock.WriteLock writeLock) {
            this.builder = builder;
            this.readLock = readLock;
            this.writeLock = writeLock;
        }

        @Override
        public Lock readLock() {
            return readLock;
        }

        @Override
        public Lock writeLock() {
            return writeLock;
        }
        
        /**
         * Returns the internal builder instance.
         * Use builder with care of read and write locks.
         * @return 
         */
        public MB getBuilder() {
            return builder;
        }
    }