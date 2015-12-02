package de.citec.jul.iface.provider;

import de.citec.jul.exception.NotAvailableException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface LabelProvider {

    public String getLabel() throws NotAvailableException;
}
