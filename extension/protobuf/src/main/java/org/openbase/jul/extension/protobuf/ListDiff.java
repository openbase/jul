package org.openbase.jul.extension.protobuf;

/*-
 * #%L
 * JUL Extension Protobuf
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

import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Identifiable;
import org.slf4j.LoggerFactory;

import java.util.List;

public class ListDiff<KEY, VALUE extends Identifiable<KEY>> {

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(ProtobufListDiff.class);

    private IdentifiableValueMap<KEY, VALUE> newValues, updatedValues, removedValues, originalValues;

    public ListDiff(final List<VALUE> originalValues) {
        this();
        this.originalValues.putAll(IdentifiableValueMap.fromCollection(originalValues));
    }

    public ListDiff(IdentifiableValueMap<KEY, VALUE> originValues) {
        this();
        this.originalValues.putAll(originValues);
    }

    public ListDiff() {
        this.newValues = new IdentifiableValueMap<>();
        this.updatedValues = new IdentifiableValueMap<>();
        this.removedValues = new IdentifiableValueMap<>();
        this.originalValues = new IdentifiableValueMap<>();
    }

    public void diff(final List<VALUE> modifiedList) {
        diff(IdentifiableValueMap.fromCollection(modifiedList));
    }

    public void diff(final List<VALUE> originalList, final List<VALUE> modifiedList) {
        diff(IdentifiableValueMap.fromCollection(originalList), IdentifiableValueMap.fromCollection(modifiedList));
    }

    public void diff(final IdentifiableValueMap<KEY, VALUE> modifiedMap) {
        diff(originalValues, modifiedMap);
    }

    public void diff(final IdentifiableValueMap<KEY, VALUE> originalMap, final IdentifiableValueMap<KEY, VALUE> modifiedMap) {
        newValues.clear();
        updatedValues.clear();
        removedValues.clear();

        final IdentifiableValueMap<KEY, VALUE> modifiedCopy = new IdentifiableValueMap<>(modifiedMap);

        originalMap.keySet().stream().forEach((id) -> {
            try {
                if (modifiedMap.containsKey(id)) {
                    if (!originalMap.get(id).equals(modifiedMap.get(id))) {
                        updatedValues.put(modifiedMap.get(id));
                    }
                    modifiedCopy.remove(id);
                } else {
                    removedValues.put(originalMap.get(id));
                }
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Ignoring invalid value[" + id + "]", ex), logger, LogLevel.ERROR);
            }
        });
        // add all messages which are not included in original list.
        newValues.putAll(modifiedCopy);

        // update original messages.
        originalValues = modifiedMap;
    }

    public final IdentifiableValueMap<KEY, VALUE> getNewValueMap() {
        return newValues;
    }

    public final IdentifiableValueMap<KEY, VALUE> getUpdatedValueMap() {
        return updatedValues;
    }

    public final IdentifiableValueMap<KEY, VALUE> getRemovedValueMap() {
        return removedValues;
    }

    public int getChangeCounter() {
        return newValues.size() + updatedValues.size() + removedValues.size();
    }

    public void replaceOriginalMap(final IdentifiableValueMap<KEY, VALUE> originalMap) {
        originalValues.clear();
        originalValues.putAll(originalMap);
    }

    public final IdentifiableValueMap<KEY, VALUE> getOriginalValueMap() {
        return originalValues;
    }
}
