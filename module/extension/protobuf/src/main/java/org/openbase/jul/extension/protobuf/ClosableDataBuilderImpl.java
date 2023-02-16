package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
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

import com.google.protobuf.AbstractMessage.Builder;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.extension.protobuf.BuilderSyncSetup.NotificationStrategy;

/**
 * @param <MB>
 */
@Deprecated
public class ClosableDataBuilderImpl<MB extends Builder<MB>> implements ClosableDataBuilder<MB> {

    private final BuilderSyncSetup<MB> builderSetup;
    private final NotificationStrategy notificationStrategy;

    public ClosableDataBuilderImpl(final BuilderSyncSetup<MB> builderSetup, final Object consumer) {
        this(builderSetup, consumer, NotificationStrategy.AFTER_LAST_RELEASE);
    }

    public ClosableDataBuilderImpl(final BuilderSyncSetup<MB> builderSetup, final Object consumer, final boolean notifyChange) {
        try {
            this.builderSetup = builderSetup;
            this.builderSetup.lockWriteInterruptibly(consumer);
            this.notificationStrategy = notifyChange ? NotificationStrategy.FORCE : NotificationStrategy.SKIP;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while initializing ClosableDataBuilder");
        }
    }

    public ClosableDataBuilderImpl(final BuilderSyncSetup<MB> builderSetup, final Object consumer, final NotificationStrategy notificationStrategy) {
        try {
            this.builderSetup = builderSetup;
            this.builderSetup.lockWriteInterruptibly(consumer);
            this.notificationStrategy = notificationStrategy;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Interrupted while initializing ClosableDataBuilder");
        }
    }

    @Override
    public MB getInternalBuilder() {
        return builderSetup.getBuilder();
    }

    @Override
    public void close() throws CouldNotPerformException {
        builderSetup.unlockWrite(notificationStrategy);
    }
}
