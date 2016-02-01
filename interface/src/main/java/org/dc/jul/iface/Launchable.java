package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author * @author <a href="mailto:DivineThreepwood@gmail.com">Divine Threepwood</a>
 */
public interface Launchable {

    public void launch() throws CouldNotPerformException, InterruptedException;
}
