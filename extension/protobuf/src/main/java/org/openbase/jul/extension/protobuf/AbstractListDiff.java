package org.openbase.jul.extension.protobuf;

/*-
 * #%L
 * JUL Extension Protobuf
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
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.exception.printer.LogLevel;
import org.openbase.jul.iface.Identifiable;
import org.openbase.jul.schedule.SyncObject;
import org.slf4j.LoggerFactory;

import java.util.List;

public abstract class AbstractListDiff<KEY, VALUE extends Identifiable<KEY>, MAP extends IdentifiableValueMap<KEY, VALUE>> {

    protected final org.slf4j.Logger logger = LoggerFactory.getLogger(getClass());

    private MAP newValues, updatedValues, removedValues, originalValues;

    private final SyncObject listDiffLock = new SyncObject("'ListDiffLock");

    public AbstractListDiff(final MAP newValues, final MAP updatedValues, final MAP removedValues, final MAP originalValues) {
        this.newValues = newValues;
        this.updatedValues = updatedValues;
        this.removedValues = removedValues;
        this.originalValues = originalValues;
    }

    public void diff(final MAP modifiedMap) {
        diff(originalValues, modifiedMap);
    }

    public void diff(final MAP originalMap, final MAP modifiedMap) {
        synchronized (listDiffLock) {
            newValues.clear();
            updatedValues.clear();
            removedValues.clear();

            final IdentifiableValueMap<KEY, VALUE> modifiedCopy = new IdentifiableValueMap<>(modifiedMap);


            for (KEY id : originalMap.keySet()) {
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
            }

            // add all messages which are not included in original list.
            newValues.putAll(modifiedCopy);

            // update original messages.
            originalValues = modifiedMap;
        }
    }

    public final MAP getNewValueMap() {
        synchronized (listDiffLock) {
            return copyMap(newValues);
        }
    }

    public final MAP getUpdatedValueMap() {
        synchronized (listDiffLock) {
            return copyMap(updatedValues);
        }
    }

    public final MAP getRemovedValueMap() {
        synchronized (listDiffLock) {
            return copyMap(removedValues);
        }
    }

    public int getChangeCounter() {
        synchronized (listDiffLock) {
            return newValues.size() + updatedValues.size() + removedValues.size();
        }
    }

    public void replaceOriginalMap(final MAP originalMap) {
        synchronized (listDiffLock) {
            originalValues.clear();
            originalValues.putAll(originalMap);
        }
    }

    public final MAP getOriginalValueMap() {
        synchronized (listDiffLock) {
            return copyMap(originalValues);
        }
    }

    public abstract void diff(final List<VALUE> modifiedList);

    public abstract void diff(final List<VALUE> originalList, final List<VALUE> modifiedList);

    protected abstract MAP copyMap(final MAP map);

}
