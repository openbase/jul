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
 * @param <UPDATE> The value type to update.
 * @param <INSTANCE> The type of instance which can be updated.
 */
public interface Updatable<UPDATE, INSTANCE extends Updatable<UPDATE, INSTANCE>>{
    public INSTANCE update(UPDATE update) throws CouldNotPerformException;
}
