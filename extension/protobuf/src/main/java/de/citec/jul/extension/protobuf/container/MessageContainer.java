package de.citec.jul.extension.protobuf.container;

import com.google.protobuf.GeneratedMessage;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M>
 */
public interface MessageContainer<M extends GeneratedMessage> {

    public M getMessage();

}
