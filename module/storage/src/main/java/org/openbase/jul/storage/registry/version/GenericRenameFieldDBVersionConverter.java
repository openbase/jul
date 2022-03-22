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
 * <p>
 * Generic converter to rename a field of a db entry.
 */
public abstract class GenericRenameFieldDBVersionConverter extends AbstractDBVersionConverter {
    private final String[] path;
    private final String newName;

    /**
     * example given: newName["water"] - path["office", "desk", "ice"] - result["office", "desk", "water"]
     *
     * @param versionControl the current db version.
     * @param newName        the new name of the field.
     * @param path           the path inclusive the old field.
     */
    public GenericRenameFieldDBVersionConverter(final DBVersionControl versionControl, final String newName, final String... path) {
        super(versionControl);
        this.path = path;
        this.newName = newName;
    }

    /**
     * Generic renaming.
     *
     * @param outdatedDBEntry the outdated db entry where the upgrade should be applied to.
     * @param dbSnapshot      all entries of the current database which may are partially upgraded.
     *
     * @return the updated value.
     */
    @Override
    public JsonObject upgrade(final JsonObject outdatedDBEntry, Map<File, JsonObject> dbSnapshot) {
        return rename(outdatedDBEntry, newName, path);
    }

    /**
     * Method to rename a field.
     *
     * @param outdatedDBEntry the root entry of the path.
     * @param newName         the new name of the field.
     * @param path            the path inclusive the old field.
     *
     * @return the updated value.
     */
    public static JsonObject rename(final JsonObject outdatedDBEntry, final String newName, final String... path) {
        // store root element
        JsonObject parent = outdatedDBEntry;

        // lookup element to rename
        for (int i = 0; i < path.length; i++) {
            final String propertyName = path[i];


            // property not found so nothing to rename
            if (!parent.has(propertyName)) {
                return outdatedDBEntry;
            }

            // if element to rename selected than rename otherwise contine with next element
            if (i == path.length - 1) {
                parent.add(newName, parent.remove(propertyName));

            } else {
                // prepare next round
                parent = parent.getAsJsonObject(propertyName);
            }
        }
        return outdatedDBEntry;
    }
}
