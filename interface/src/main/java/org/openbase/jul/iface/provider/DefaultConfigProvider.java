package org.openbase.jul.iface.provider;

import org.openbase.jul.exception.NotAvailableException;

/**
 * #%L
 * #L%
 *
 * @author <a href="mailto:divine@openbase.org">Divine Threepwood</a>
 * @param <CONFIG>
 */
public interface DefaultConfigProvider<CONFIG> {

    public CONFIG getDefaultConfig() throws NotAvailableException;
}
