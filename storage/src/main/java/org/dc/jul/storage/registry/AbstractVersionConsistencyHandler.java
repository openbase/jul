package org.dc.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * $Id:$
 * $HeadURL:$
 * %%
 * Copyright (C) 2015 - 2016 DivineCooperation
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

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.storage.registry.version.DBVersionControl;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractVersionConsistencyHandler<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractProtoBufRegistryConsistencyHandler<KEY, M, MB> {

    protected final DBVersionControl versionControl;
    protected final FileSynchronizedRegistryInterface<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> registry;

    public AbstractVersionConsistencyHandler(final DBVersionControl versionControl, final FileSynchronizedRegistryInterface<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> registry) throws org.dc.jul.exception.InstantiationException {
        this.versionControl = versionControl;
        this.registry = registry;
    }

    @Override
    public boolean shutdown() {
        if(registry.isConsistent()) {
            try {
                versionControl.registerConsistencyHandlerExecution(this);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, logger);
            }
        }
        return super.shutdown();
    }
}
