/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.extension.rsb.iface;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.NotAvailableException;
import java.util.Collection;
import rsb.patterns.Callback;
import rsb.patterns.Method;

/**
 *
 * @author Divine <a href="mailto:DivineThreepwood@gmail.com">Divine</a>
 */
public interface RSBServerInterface extends RSBParticipantInterface {

    public Collection<? extends Method> getMethods() throws NotAvailableException;

    public Method getMethod(String name) throws NotAvailableException;

    public boolean hasMethod(String name);
}
