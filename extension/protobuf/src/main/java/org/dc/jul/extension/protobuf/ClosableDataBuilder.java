/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage.Builder;

/**
 *
 * @param <MB>
 */
public class ClosableDataBuilder<MB extends Builder<MB>> implements java.lang.AutoCloseable {

        private final BuilderSyncSetup<MB> builderSetup;

        public ClosableDataBuilder(final BuilderSyncSetup<MB> builderSetup, final Object consumer) {
            this.builderSetup = builderSetup;
            builderSetup.lockWrite(consumer);
        }

        public MB getInternalBuilder() {
            return builderSetup.getBuilder();
        }

        @Override
        public void close() throws Exception {
            builderSetup.unlockWrite();
        }
    }