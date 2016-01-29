/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:thuxohl@techfak.uni-bielefeld.com">Tamino Huxohl</a>
 */
public interface Enableable {

    public void enable() throws CouldNotPerformException, InterruptedException;

    public void disable() throws CouldNotPerformException, InterruptedException;

    public boolean isEnabled();
}
