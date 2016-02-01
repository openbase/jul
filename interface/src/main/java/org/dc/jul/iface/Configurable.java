package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.iface.provider.ConfigProvider;

/**
 *
 * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 *
 * @param <ID> The id type of the configurable instance.
 * @param <CONFIG> The configuration type.
 */
public interface Configurable<ID, CONFIG> extends Identifiable<ID>, ConfigProvider<CONFIG> {

    public CONFIG updateConfig(final CONFIG config) throws CouldNotPerformException;

}
