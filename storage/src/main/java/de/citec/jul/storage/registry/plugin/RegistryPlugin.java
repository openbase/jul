/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;

/**
 *
 * @author <a href="mailto:MarianPohling@cit-ec.uni-bielefeld.de">mpohling</a>
 */
public interface RegistryPlugin<ENTRY> {

    public void beforeRegister(final ENTRY entry);
    
    public void afterRegister(final ENTRY entry);
    
    public void init() throws CouldNotPerformException;
    
    public void shutdown();
}
