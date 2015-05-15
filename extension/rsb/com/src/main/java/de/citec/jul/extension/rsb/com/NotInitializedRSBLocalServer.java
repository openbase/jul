/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBLocalServerInterface;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import rsb.Scope;
import rsb.patterns.Callback;

/**
 *
 * @author Divine <DivineThreepwood@gmail.com>
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
