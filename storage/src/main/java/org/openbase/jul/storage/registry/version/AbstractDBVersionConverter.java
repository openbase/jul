package org.openbase.jul.storage.registry.version;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2021 openbase.org
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
import java.util.Map;
import java.util.Map.Entry;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public abstract class AbstractDBVersionConverter implements DBVersionConverter {

    private final DBVersionControl versionControl;

    public AbstractDBVersionConverter(DBVersionControl versionControl) {
        this.versionControl = versionControl;
    }

    @Override
    public DBVersionControl getVersionControl() {
        return versionControl;
    }

    protected void removeFromDBSnapshot(final JsonObject jsonObject, final Map<File, JsonObject> dbSnapshot) throws CouldNotPerformException {
        File file = null;
        for(Entry<File, JsonObject> entry : dbSnapshot.entrySet()) {
            if(entry.getValue().equals(jsonObject)) {
                file = entry.getKey();
            }
        }

        if(file == null) {
            throw new CouldNotPerformException("Could not find file for entry["+jsonObject+"]");
        }

        dbSnapshot.remove(file);
    }
}
