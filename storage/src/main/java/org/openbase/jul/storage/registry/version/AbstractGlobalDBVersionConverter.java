package org.openbase.jul.storage.registry.version;

/*-
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

import com.google.gson.JsonObject;
import org.openbase.jul.exception.CouldNotPerformException;

import java.io.File;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

/**
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public abstract class AbstractGlobalDBVersionConverter extends AbstractDBVersionConverter implements GlobalDBVersionConverter {

    public AbstractGlobalDBVersionConverter(DBVersionControl versionControl) {
        super(versionControl);
    }

    protected void removeFromGlobalDBSnapshot(final JsonObject remove, final String databaseKey,
                                           final Map<String, Map<File, DatabaseEntryDescriptor>> globalDbSnapshots)
            throws CouldNotPerformException {
        // test if database key is valid
        if (!globalDbSnapshots.containsKey(databaseKey)) {
            String keys = "";
            for (Iterator<String> keyIterator = globalDbSnapshots.keySet().iterator(); keyIterator.hasNext(); ) {
                String key = keyIterator.next();

                keys += key;
                if (keyIterator.hasNext()) {
                    keys += ", ";
                }
            }
            throw new CouldNotPerformException("Invalid database key[" + databaseKey + "] valid keys are [" + keys + "]");
        }

        // find according file
        File file = null;
        for (Entry<File, DatabaseEntryDescriptor> entry : globalDbSnapshots.get(databaseKey).entrySet()) {
            if (entry.getValue().getEntry().equals(remove)) {
                // file found
                file = entry.getKey();
                break;
            }
        }

        // file could not be found
        if (file == null) {
            throw new CouldNotPerformException("Could not find File of [" + remove + "]");
        }

        // remove file
        globalDbSnapshots.get(databaseKey).remove(file);
    }

}
