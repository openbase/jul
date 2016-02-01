package org.dc.jul.extension.rst.iface;

import org.dc.jul.exception.NotAvailableException;
import rst.rsb.ScopeType.Scope;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface ScopeProvider {

    public Scope getScope() throws NotAvailableException;
}
