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
 * @author <a href="mailto:MarianPohling@cit-ec.uni-bielefeld.de">mpohling</a>
 */
public interface FileRegistryPlugin extends RegistryPlugin {

    public void beforeRegister(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeUpdate(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeRemove(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeGet(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void beforeGetEntries() throws CouldNotPerformException;

    public void beforeClear() throws CouldNotPerformException;

    public void afterRegister(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void afterUpdate(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void afterRemove(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void afterGet(final FileSynchronizer fileSynchronizer) throws CouldNotPerformException;

    public void afterGetEntries() throws CouldNotPerformException;

    public void afterClear() throws CouldNotPerformException;

    public void checkAccess() throws InvalidStateException;

}
