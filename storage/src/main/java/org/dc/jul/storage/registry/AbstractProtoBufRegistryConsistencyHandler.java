package org.dc.jul.storage.registry;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapInterface;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractProtoBufRegistryConsistencyHandler<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> extends AbstractConsistencyHandler<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMapInterface<KEY, M, MB>, ProtoBufRegistryInterface<KEY, M, MB>> implements ProtoBufRegistryConsistencyHandler<KEY, M, MB> {

}
