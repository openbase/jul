package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2018 openbase.org
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
import com.google.protobuf.AbstractMessage;
import com.google.protobuf.Message;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.extension.protobuf.IdentifiableMessage;
import org.openbase.jul.storage.registry.version.DBVersionControl;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @deprecated because version consistency handler is not rst independent which does not allow a stable converter pipeline. Please use the global db version converter for this purpose.
 */
@Deprecated
public abstract class AbstractVersionConsistencyHandler<KEY extends Comparable<KEY>, M extends AbstractMessage, MB extends M.Builder<MB>> extends AbstractProtoBufRegistryConsistencyHandler<KEY, M, MB> {

    protected final DBVersionControl versionControl;
    protected final FileSynchronizedRegistry<KEY, IdentifiableMessage<KEY, M, MB>> registry;

    public AbstractVersionConsistencyHandler(final DBVersionControl versionControl, final ProtoBufRegistry<KEY, M, MB> registry) throws org.openbase.jul.exception.InstantiationException {
        this.versionControl = versionControl;
        this.registry = registry;
    }

    @Override
    public void shutdown() {
        if (registry.isConsistent() && !registry.isSandbox()) {
            try {
                versionControl.registerConsistencyHandlerExecution(this);
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(ex, logger);
            }
        }
        super.shutdown();
    }
}
