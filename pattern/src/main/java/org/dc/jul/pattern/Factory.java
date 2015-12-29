/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.pattern;

import org.dc.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * 
 * Factory pattern interface.
 * 
 * @param <INSTANCE> Type of instance which can be created by using this factory.
 * @param <CONFIG> Configuration type which contains all attributes to create a new instance.
 */
public interface Factory<INSTANCE, CONFIG> {

    /**
     * Creates a new instance with the given configuration.
     * @param config
     * @return 
     * @throws org.dc.jul.exception.CouldNotPerformException 
     */
    public INSTANCE newInstance(final CONFIG config) throws CouldNotPerformException;
}
