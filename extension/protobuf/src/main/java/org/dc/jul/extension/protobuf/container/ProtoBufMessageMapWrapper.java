/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.protobuf.container;

import com.google.protobuf.GeneratedMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.protobuf.IdentifiableMessage;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @param <SIB>
 */
public class ProtoBufMessageMapWrapper<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder<SIB>> extends HashMap<KEY, IdentifiableMessage<KEY, M, MB>> implements ProtoBufMessageMapInterface<KEY, M, MB> {

    public ProtoBufMessageMapWrapper() {
    }

    public ProtoBufMessageMapWrapper(final ProtoBufMessageMapInterface<KEY, M, MB> entryMap) {
        putAll(entryMap);
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> put(IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException {
        return super.put(value.getId(), value);
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> get(KEY key) throws CouldNotPerformException {
        return super.get(key);
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> get(IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException {
        return super.get(value.getId());
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        ArrayList<M> list = new ArrayList<>();
        values().stream().forEach((identifiableMessage) -> {
            list.add(identifiableMessage.getMessage());
        });
        return list;
    }

    @Override
    public M getMessage(KEY key) throws CouldNotPerformException {
        return get(key).getMessage();
    }
}
