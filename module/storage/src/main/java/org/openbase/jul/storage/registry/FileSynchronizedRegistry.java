package org.openbase.jul.storage.registry;

/*
 * #%L
 * JUL Storage
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
import org.openbase.jul.exception.NotAvailableException;
import org.openbase.jul.iface.Identifiable;

import java.io.File;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <KEY>
 * @param <VALUE>
 */
public interface FileSynchronizedRegistry<KEY, VALUE extends Identifiable<KEY>> extends Registry<KEY, VALUE> {

    void loadRegistry() throws CouldNotPerformException;

    void saveRegistry() throws CouldNotPerformException;

    Integer getDBVersion() throws NotAvailableException;

    File getDatabaseDirectory() throws NotAvailableException;

    boolean isLocalRegistry();

    enum DatabaseState {
        UNKNOWN,
        LOADING,
        OUTDATED,
        LATEST
    }
}
