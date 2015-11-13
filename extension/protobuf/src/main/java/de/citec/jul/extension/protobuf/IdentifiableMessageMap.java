package de.citec.jul.extension.protobuf;

import com.google.protobuf.GeneratedMessage;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <KEY>
 * @param <M>
 * @param <MB>
 */
public class IdentifiableMessageMap<KEY, M extends GeneratedMessage, MB extends M.Builder<MB>> extends IdentifiableValueMap<KEY, IdentifiableMessage<KEY, M, MB>> {

    public IdentifiableMessageMap(List<M> messageList) {

        if (messageList == null) {
            return;
        }

        for (M message : messageList) {
            try {
                put(new IdentifiableMessage<>(message));
            } catch (CouldNotPerformException ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not add Message[" + message + "] to message map!", ex), logger);
            }
        }
    }

    public IdentifiableMessageMap(int initialCapacity, float loadFactor) {
        super(initialCapacity, loadFactor);
    }

    public IdentifiableMessageMap(int initialCapacity) {
        super(initialCapacity);
    }

    public IdentifiableMessageMap() {
    }

    public IdentifiableMessageMap(Map<? extends KEY, ? extends IdentifiableMessage<KEY, M, MB>> m) {
        super(m);
    }

    public Set<M> getMessages() {
        Set<M> messages = new HashSet<>();
        for (IdentifiableMessage<KEY, M, MB> identifiableMessage : values()) {
            messages.add(identifiableMessage.getMessage());
        }
        return messages;
    }
}
