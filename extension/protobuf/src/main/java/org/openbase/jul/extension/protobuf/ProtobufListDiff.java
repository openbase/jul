package org.openbase.jul.extension.protobuf;

/*
 * #%L
 * JUL Extension Protobuf
 * %%
 * Copyright (C) 2015 - 2019 openbase.org
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

import com.google.protobuf.AbstractMessage;
import org.openbase.jul.exception.CouldNotPerformException;
import org.openbase.jul.exception.printer.ExceptionPrinter;
import org.openbase.jul.iface.Identifiable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 */
public class ProtobufListDiff<KEY, M extends AbstractMessage, MB extends M.Builder<MB>> extends AbstractListDiff<KEY, IdentifiableMessage<KEY, M, MB>, IdentifiableMessageMap<KEY, M, MB>> {

    public ProtobufListDiff(final List<M> originalMessages) {
        this();
        replaceOriginalMap(new IdentifiableMessageMap<>(originalMessages));
    }

    public ProtobufListDiff(IdentifiableMessageMap<KEY, M, MB> originalMessages) {
        this();
        replaceOriginalMap(new IdentifiableMessageMap<>(originalMessages));
    }

    public ProtobufListDiff() {
        super(new IdentifiableMessageMap<>(), new IdentifiableMessageMap<>(), new IdentifiableMessageMap<>(),  new IdentifiableMessageMap<>());
    }

    @Override
    protected IdentifiableMessageMap<KEY, M, MB> copyMap(IdentifiableMessageMap<KEY, M, MB> map) {
        return new IdentifiableMessageMap<>(map);
    }

    public void diffMessages(final List<M> modifiedList) {
        diff(new IdentifiableMessageMap<>(modifiedList));
    }

    public void diffMessages(final List<M> originalList, final List<M> modifiedList) {
        diff(new IdentifiableMessageMap<>(originalList), new IdentifiableMessageMap<>(modifiedList));
    }

    @Override
    public void diff(final List<IdentifiableMessage<KEY, M, MB>> modifiedList) {
        diff(new IdentifiableMessageMap<>(IdentifiableValueMap.fromCollection(modifiedList)));
    }

    @Override
    public void diff(final List<IdentifiableMessage<KEY, M, MB>> originalList, final List<IdentifiableMessage<KEY, M, MB>> modifiedList) {
        diff(new IdentifiableMessageMap<>(IdentifiableValueMap.fromCollection(originalList)), new IdentifiableMessageMap<>(IdentifiableValueMap.fromCollection(modifiedList)));
    }

    public IdentifiableMessageMap<KEY, M, MB> getNewMessageMap() {
        return getNewValueMap();
    }

    public IdentifiableMessageMap<KEY, M, MB> getUpdatedMessageMap() {
        return getUpdatedValueMap();
    }

    public IdentifiableMessageMap<KEY, M, MB> getRemovedMessageMap() {
        return getRemovedValueMap();
    }

    /**
     *
     * @param originMap
     * @deprecated since v2.0 and will be removed in v3.0. Please use replaceOriginalMap instead, will be removed in 3.0
     */
    @Deprecated
    public void replaceOriginMap(IdentifiableMessageMap<KEY, M, MB> originMap) {
        replaceOriginalMap(originMap);
    }

    /**
     *
     * @return
     * @deprecated since v2.0 and will be removed in v3.0. Please use getOriginalMessageMap instead.
     */
    @Deprecated
    public IdentifiableMessageMap<KEY, M, MB> getOriginMessages() {
        return getOriginalMessages();
    }

    public IdentifiableMessageMap<KEY, M, MB> getOriginalMessages() {
        return getOriginalValueMap();
    }
}
