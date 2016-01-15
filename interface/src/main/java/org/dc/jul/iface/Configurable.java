package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;

/**
 *
 * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 *
 * @param <ID> The id type of the configurable instance.
 * @param <CONFIG> The configuration type.
 * @param <INSTANCE> The configurable instance type.
 */
public interface Configurable<ID, CONFIG, INSTANCE extends Configurable<ID, CONFIG, INSTANCE>> extends Identifiable<ID>, Updatable<CONFIG, INSTANCE> {

    public CONFIG getConfig() throws NotAvailableException;

    @Override
    public INSTANCE update(final CONFIG config) throws CouldNotPerformException;
    
}
