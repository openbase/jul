package de.citec.jul.storage.registry.version;

import com.google.gson.JsonObject;
import de.citec.jul.exception.CouldNotPerformException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 *
 * This converter transforms the outdated db entry into a new db version entry.
 */
public interface DBVersionConverter {

    public JsonObject upgrade(JsonObject outdatedDBEntry) throws CouldNotPerformException;
}
