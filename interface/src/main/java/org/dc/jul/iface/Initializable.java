package org.dc.jul.iface;

import org.dc.jul.exception.InitializationException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <CONFIG>
 */
public interface Initializable<CONFIG> {
    public void init(final CONFIG config) throws InitializationException, InterruptedException;
}
