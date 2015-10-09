/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.storage.registry.RegistryInterface;

/**
 *
 * @author <a href="mailto:MarianPohling@cit-ec.uni-bielefeld.de">mpohling</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public interface RegistryPlugin<KEY, ENTRY extends Identifiable<KEY>> {

    public void init(final RegistryInterface<KEY, ENTRY, ?> registry) throws CouldNotPerformException;

    public void shutdown();

    public void beforeRegister(final ENTRY entry) throws RejectedException;

    public void afterRegister(final ENTRY entry) throws CouldNotPerformException;

    public void beforeUpdate(final ENTRY entry) throws RejectedException;

    public void afterUpdate(final ENTRY entry) throws CouldNotPerformException;

    public void beforeRemove(final ENTRY entry) throws RejectedException;

    public void afterRemove(final ENTRY entry) throws CouldNotPerformException;

    public void beforeClear() throws CouldNotPerformException;

    public void beforeGet(final KEY key) throws RejectedException;

    public void beforeGetEntries() throws CouldNotPerformException;

    public void checkAccess() throws RejectedException;
}
