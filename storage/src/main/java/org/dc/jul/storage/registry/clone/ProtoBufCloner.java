package org.dc.jul.storage.registry.clone;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.extension.protobuf.IdGenerator;
import org.dc.jul.extension.protobuf.IdentifiableMessage;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapInterface;
import org.dc.jul.extension.protobuf.container.ProtoBufMessageMapWrapper;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class ProtoBufCloner<KEY extends Comparable<KEY>, M extends GeneratedMessage, MB extends M.Builder<MB>> implements RegistryCloner<KEY, IdentifiableMessage<KEY, M, MB>, ProtoBufMessageMapInterface<KEY, M, MB>> {

    private final IdGenerator<KEY, M> idGenerator;

    public ProtoBufCloner(IdGenerator<KEY, M> idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public Map<KEY, IdentifiableMessage<KEY, M, MB>> deepCloneMap(Map<KEY, IdentifiableMessage<KEY, M, MB>> map) throws CouldNotPerformException {
        try {
            HashMap<KEY, IdentifiableMessage<KEY, M, MB>> mapClone = new HashMap<>();

            for (Map.Entry<KEY, IdentifiableMessage<KEY, M, MB>> entry : map.entrySet()) {
                mapClone.put(entry.getKey(), deepCloneEntry(entry.getValue()));
            }
            return mapClone;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not clone registry map!", ex);
        }
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> deepCloneEntry(IdentifiableMessage<KEY, M, MB> entry) throws CouldNotPerformException {
        try {
            return new IdentifiableMessage<>(entry.getMessage(), idGenerator);
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not clone Entry!", ex);
        }
    }

    @Override
    public ProtoBufMessageMapInterface<KEY, M, MB> deepCloneRegistryMap(ProtoBufMessageMapInterface<KEY, M, MB> map) throws CouldNotPerformException {
        try {
            ProtoBufMessageMapInterface<KEY, M, MB> mapClone = new ProtoBufMessageMapWrapper<>();

            for (Map.Entry<KEY, IdentifiableMessage<KEY, M, MB>> entry : map.entrySet()) {
                mapClone.put(entry.getKey(), deepCloneEntry(entry.getValue()));
            }
            return mapClone;
        } catch (CouldNotPerformException ex) {
            throw new CouldNotPerformException("Could not clone registry map!", ex);
        }
    }
}
