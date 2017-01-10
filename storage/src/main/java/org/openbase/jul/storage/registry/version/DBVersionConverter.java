package org.openbase.jul.storage.registry.version;

/*
 * #%L
 * JUL Storage
 * %%
 * Copyright (C) 2015 - 2017 openbase.org
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
import java.util.Map;
import org.openbase.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 *
 * This converter transforms the outdated db entry into a new db version entry.
 */
public interface DBVersionConverter {

    /**
     * This method applies an upgrade transaction on the given entry to push the entry to the next db version.
     *
     * @param outdatedDBEntry the outdated db entry where the upgrade should be applied to.
     * @param dbSnapshot all entries of the current database which may are partially upgraded.
     * @return the upgraded database entry.
     * @throws CouldNotPerformException can be thrown in case the upgrade could not be performed.
     */
    public JsonObject upgrade(final JsonObject outdatedDBEntry, final Map<File, JsonObject> dbSnapshot) throws CouldNotPerformException;

    public DBVersionControl getVersionControl();
}
