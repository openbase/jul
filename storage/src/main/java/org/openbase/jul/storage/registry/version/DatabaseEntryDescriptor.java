package org.openbase.jul.storage.registry.version;

/*-
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2016 openbase.org
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
import java.io.File;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class DatabaseEntryDescriptor {

    private final JsonObject entry;
    private File entryFile;
    private int version;
    private final File databaseDirectory;

    public DatabaseEntryDescriptor(JsonObject entry, final DBVersionControl versionControl) {
        this.entry = entry;
        databaseDirectory = versionControl.getDatabaseDirectory();
    }

    public DatabaseEntryDescriptor(JsonObject entry, File entryFile, int version, File databaseDirectory) {
        this.entry = entry;
        this.entryFile = entryFile;
        this.version = version;
        this.databaseDirectory = databaseDirectory;
    }

    public JsonObject getEntry() {
        return entry;
    }

    public File getEntryFile() {
        return entryFile;
    }

    public int getVersion() {
        return version;
    }

    public File getDatabaseDirectory() {
        return databaseDirectory;
    }
}
