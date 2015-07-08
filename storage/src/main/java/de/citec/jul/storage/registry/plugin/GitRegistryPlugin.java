/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.storage.file.FileSynchronizer;
import de.citec.jul.storage.registry.FileSynchronizedRegistry;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;

/**
 *
 * @author mpohling
 */
public class GitRegistryPlugin extends FileRegistryPluginAdapter {

    private final FileSynchronizedRegistry registry;
    private final Repository repository;
    private final Git git;

    public GitRegistryPlugin(FileSynchronizedRegistry registry) throws de.citec.jul.exception.InstantiationException {
        try {
            this.registry = registry;
            this.repository = new FileRepository(new File(registry.getDatabaseDirectory(), ".git"));
            this.git = new Git(repository);
        } catch (Exception ex) {
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }

    @Override
    public void afterRegister(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    @Override
    public void afterUpdate(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    @Override
    public void afterRemove(FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges(); 
    }

    @Override
    public void afterClear() throws CouldNotPerformException {
        commitAllChanges();
    }

    private void commitAllChanges() throws CouldNotPerformException {
        try {
            // add all changes
            git.add().addFilepattern(".").call();

            // commit
            git.commit().call();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not commit all database changes!", ex);
        }
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        try {
            if (repository.getBranch() == "master") {
                throw new InvalidStateException("Database tag can not be modifiered!");
            }
        } catch (IOException ex) {
            throw new InvalidStateException("Could not access database!", ex);
        }
    }
}
