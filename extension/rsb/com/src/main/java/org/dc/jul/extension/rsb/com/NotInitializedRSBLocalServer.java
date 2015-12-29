/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.extension.rsb.com;

import org.dc.jul.extension.rsb.iface.RSBLocalServerInterface;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.InvalidStateException;
import rsb.Scope;
import rsb.patterns.Callback;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public class NotInitializedRSBLocalServer extends NotInitializedRSBServer implements RSBLocalServerInterface {

    public NotInitializedRSBLocalServer() {
    }

    public NotInitializedRSBLocalServer(Scope scope) {
        super(scope);
    }

    @Override
    public void addMethod(String name, Callback callback) throws CouldNotPerformException {
        throw new CouldNotPerformException("Could not add Method["+name+"]!", new InvalidStateException("LocalServer not initialized!"));
    }

    @Override
    public void waitForShutdown() throws CouldNotPerformException, InterruptedException {
        return;
    }
}
