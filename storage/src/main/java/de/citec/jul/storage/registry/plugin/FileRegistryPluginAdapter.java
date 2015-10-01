/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.RejectedException;
import de.citec.jul.iface.Identifiable;
import de.citec.jul.storage.file.FileSynchronizer;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 * @param <KEY>
 * @param <ENTRY>
 */
public abstract class FileRegistryPluginAdapter<KEY, ENTRY extends Identifiable<KEY>> extends RegistryPluginAdapter<KEY, ENTRY> implements FileRegistryPlugin<KEY, ENTRY> {

    @Override
    public void beforeRegister(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
    }

    @Override
    public void afterRegister(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeRemove(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
    }

    @Override
    public void afterRemove(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeUpdate(ENTRY entry, FileSynchronizer fileSynchronizer) throws RejectedException {
    }

    @Override
    public void afterUpdate(ENTRY entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeGet(KEY key, FileSynchronizer fileSynchronizer) throws RejectedException {
    }
}
