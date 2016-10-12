package org.openbase.jul.storage.registry.version;

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
    private File databaseDirectory;

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
