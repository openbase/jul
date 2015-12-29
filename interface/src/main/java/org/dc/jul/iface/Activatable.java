/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.iface;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author Divine Threepwood
 */
public interface Activatable {

	public void activate() throws CouldNotPerformException, InterruptedException;

	public void deactivate() throws CouldNotPerformException, InterruptedException;

	public boolean isActive();
}