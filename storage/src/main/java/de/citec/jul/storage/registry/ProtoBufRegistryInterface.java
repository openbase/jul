/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.rsb.IdGenerator;
import de.citec.jul.rsb.IdentifiableMessage;
import java.util.Map;

/**
 *
 * @author mpohling
 * @param <KEY>
 * @param <M>
 * @param <MB>
 * @param <SIB>
 */
public interface ProtoBufRegistryInterface<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>, SIB extends GeneratedMessage.Builder> extends FileSynchronizedRegistryInterface<KEY, IdentifiableMessage<KEY, M>, Map<KEY, IdentifiableMessage<KEY, M>>, ProtoBufRegistryInterface<KEY, M, MB, SIB>> {

    public M register(final M entry) throws CouldNotPerformException ;
    
    public boolean contains(final M key) throws CouldNotPerformException ;

    public M update(final M entry) throws CouldNotPerformException ;

    public M remove(final M entry) throws CouldNotPerformException ;

    public M getMessage(final KEY key) throws CouldNotPerformException;

    public MB getBuilder(final KEY key) throws CouldNotPerformException;
    
    public IdGenerator<KEY, M> getIdGenerator();
}
