/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.iface;

import org.dc.jul.exception.CouldNotPerformException;
import rsb.patterns.Callback;

/**
 *
 * @author mpohling
 */
public interface RSBLocalServerInterface extends RSBServerInterface {

    public void addMethod(String name, Callback callback) throws CouldNotPerformException;

    public void waitForShutdown() throws CouldNotPerformException, InterruptedException;
}
