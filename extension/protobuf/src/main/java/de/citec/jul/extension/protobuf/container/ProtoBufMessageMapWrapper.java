/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.protobuf.container;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.extension.protobuf.IdGenerator;
import de.citec.jul.extension.protobuf.IdentifiableMessage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

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
    public IdentifiableMessage<KEY, M, MB> get(M message, IdGenerator<KEY, M> idGenerator) throws CouldNotPerformException {
        return super.get(idGenerator.generateId(message));
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> get(IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException {
        return super.get(value.getId());
    }

    @Override
    public List<M> getMessages() throws CouldNotPerformException {
        return new ArrayList(values());
    }

    @Override
    public M getMessage(KEY key) throws CouldNotPerformException {
        return get(key).getMessage();
    }
}
