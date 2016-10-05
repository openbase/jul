/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.openbase.jul.storage.registry.version;

import com.google.gson.JsonObject;
import java.io.File;
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * This converter transforms the outdated db entry into a new db version entry.
 * Additionally this global db converter has access to all other databases as well.
 * This allows globally database transfer and modifications during the version upgrade if needed.
 */
public interface GlobalDbVersionConverter extends DBVersionConverter {

    /**
     * This method applies an update transaction on the given entry to push the entry to the next db version.
     *
     * @param outdatedDBEntry the outdated db entry where the upgrade should be applied to.
     * @param dbSnapshot all entries of the current database which may are partially upgraded.
     * @param globalDbSnapshots a map of the globally available databases to apply global database transactions. The key of the map is the filename of the global database.
     * @return the upgraded database entry.
     * @throws CouldNotPerformException can be thrown in case the upgrade could not be performed.
     */
    default public JsonObject upgrade(final JsonObject outdatedDBEntry, final Map<File, JsonObject> dbSnapshot, final Map<String, Map<File, JsonObject>> globalDbSnapshots) throws CouldNotPerformException {
        return upgrade(outdatedDBEntry, dbSnapshot);
    }
}
