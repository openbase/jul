package org.dc.jul.iface.provider;

import org.dc.jul.exception.NotAvailableException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface LabelProvider {

    public String getLabel() throws NotAvailableException;
}
