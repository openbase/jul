/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.storage.file.FileSynchronizer;

/**
 *
 * @author <a href="mailto:mpohling@cit-ec.uni-bielefeld.de">Divine Threepwood</a>
 */
public class FileRegistryPluginAdapter implements FileRegistryPlugin {

    @Override
    public void beforeRegister(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeUpdate(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeRemove(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeGet(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void beforeGetEntries() throws CouldNotPerformException{
    }

    @Override
    public void beforeClear() throws CouldNotPerformException{
    }

    @Override
    public void afterRegister(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void afterUpdate(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void afterRemove(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void afterGet(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
    }

    @Override
    public void afterGetEntries() throws CouldNotPerformException{
    }

    @Override
    public void afterClear() throws CouldNotPerformException{
    }

    @Override
    public void checkAccess() throws InvalidStateException {
    }
    
}
