package de.citec.jul.extension.protobuf.container.transformer;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotTransformException;
import de.citec.jul.extension.protobuf.IdGenerator;
import de.citec.jul.extension.protobuf.IdentifiableMessage;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class IdentifiableMessageTransformer<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends MessageTransformer<IdentifiableMessage<KEY, M, MB>, M, MB> {

    private final IdGenerator<KEY, M> idGenerator;

    public IdentifiableMessageTransformer(final Class<M> messageClass, final IdGenerator<KEY, M> idGenerator) {
        super(messageClass);
        this.idGenerator = idGenerator;
    }

    @Override
    public IdentifiableMessage<KEY, M, MB> transform(final M message) throws CouldNotTransformException {
        try {
            return new IdentifiableMessage<>(message, idGenerator);
        } catch(de.citec.jul.exception.InstantiationException ex) {
            throw new CouldNotTransformException("Given message is invalid!" , ex);
        }
    }
}
