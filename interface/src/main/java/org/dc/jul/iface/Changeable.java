/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public interface Changeable {

    public void notifyChange() throws CouldNotPerformException;
}
