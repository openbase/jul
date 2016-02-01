package org.dc.jul.iface.provider;

import org.dc.jul.exception.NotAvailableException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 * @param <CONFIG>
 */
public interface ConfigProvider<CONFIG> {
    public CONFIG getConfig() throws NotAvailableException;
}
