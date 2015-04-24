/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.rsb.container;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.rsb.util.IdGenerator;
import java.util.List;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public interface ProtoBufMessageMapInterface<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends Map<KEY, IdentifiableMessage<KEY, M, MB>> {
    
    public IdentifiableMessage<KEY, M, MB> put(final IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException;
    
    public IdentifiableMessage<KEY, M, MB> get(final KEY key) throws CouldNotPerformException;
    
    public IdentifiableMessage<KEY, M, MB> get(final M message, final IdGenerator<KEY, M> idGenerator) throws CouldNotPerformException;
    
    public IdentifiableMessage<KEY, M, MB> get(final IdentifiableMessage<KEY, M, MB> value) throws CouldNotPerformException;

    public List<M> getMessages() throws CouldNotPerformException;
    
    public M getMessage(KEY key) throws CouldNotPerformException;
}
