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

import org.openbase.jul.iface.Identifiable;

import java.util.List;

public class ListDiffImpl<KEY, VALUE extends Identifiable<KEY>> extends  AbstractListDiff<KEY, VALUE, IdentifiableValueMap<KEY, VALUE>> {

    public ListDiffImpl(final List<VALUE> originalValues) {
        this();
        replaceOriginalMap(IdentifiableValueMap.fromCollection(originalValues));
    }

    public ListDiffImpl(IdentifiableValueMap<KEY, VALUE> originValues) {
        this();
        replaceOriginalMap(originValues);
    }

    public ListDiffImpl() {
        super(new IdentifiableValueMap<>(), new IdentifiableValueMap<>(), new IdentifiableValueMap<>(), new IdentifiableValueMap<>());
    }

    @Override
    protected IdentifiableValueMap<KEY, VALUE> copyMap(IdentifiableValueMap<KEY, VALUE> map) {
        return new IdentifiableValueMap<>(map);
    }

    @Override
    public void diff(final List<VALUE> modifiedList) {
        diff(IdentifiableValueMap.fromCollection(modifiedList));
    }

    @Override
    public void diff(final List<VALUE> originalList, final List<VALUE> modifiedList) {
        diff(IdentifiableValueMap.fromCollection(originalList), IdentifiableValueMap.fromCollection(modifiedList));
    }
}
