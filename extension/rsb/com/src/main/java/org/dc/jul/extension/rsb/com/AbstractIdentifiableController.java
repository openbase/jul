package org.dc.jul.extension.rsb.com;

import com.google.protobuf.GeneratedMessage;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InstantiationException;
import org.dc.jul.iface.Identifiable;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <M>
 * @param <MB>
 */
public abstract class AbstractIdentifiableController<M extends GeneratedMessage, MB extends M.Builder<MB>> extends RSBCommunicationService<M, MB> implements Identifiable<String> {

    public AbstractIdentifiableController(MB builder) throws InstantiationException {
        super(builder);
    }

    @Override
    public String getId() throws CouldNotPerformException {
        return (String) getField(FIELD_ID);
    }

}
