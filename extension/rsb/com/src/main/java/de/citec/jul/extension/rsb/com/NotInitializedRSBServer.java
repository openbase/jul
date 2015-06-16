/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.com;

import de.citec.jul.extension.rsb.iface.RSBServerInterface;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import java.util.Collection;
import rsb.Scope;
import rsb.patterns.Method;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public abstract class NotInitializedRSBServer extends NotInitializedRSBParticipant implements RSBServerInterface {

    public NotInitializedRSBServer() {
    }

    public NotInitializedRSBServer(Scope scope) {
        super(scope);
    }

    @Override
    public Collection<? extends Method> getMethods() throws NotAvailableException {
        throw new NotAvailableException("methods", new InvalidStateException("Server not initialized!"));
    }

    @Override
    public Method getMethod(String name) throws NotAvailableException {
        throw new NotAvailableException("Method["+name+"]", new InvalidStateException("Server not initialized!"));
    }

    @Override
    public boolean hasMethod(String name) {
        return false;
    }

}
