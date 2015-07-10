/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jps.core.JPService;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.storage.file.FileSynchronizer;
import de.citec.jul.storage.jp.JPInitializeDB;
import de.citec.jul.storage.registry.FileSynchronizedRegistry;
import java.io.File;
import java.io.IOException;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;

/**
 *
 * @author mpohling //
 */
public class GitRegistryPlugin extends FileRegistryPluginAdapter {

    private final FileSynchronizedRegistry registry;
    private final Repository repository;
    private final Git git;

    public GitRegistryPlugin(FileSynchronizedRegistry registry) throws de.citec.jul.exception.InstantiationException {
        try {
            this.registry = registry;
            this.repository = detectRepository(registry.getDatabaseDirectory());
            this.git = new Git(repository);

        } catch (Exception ex) {
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }

    private Repository detectRepository(final File databaseDirectory) throws CouldNotPerformException {
        try {
            Repository repository;
            try {
                FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder().findGitDir(databaseDirectory);

                if (repositoryBuilder.getGitDir() == null) {
                    throw new NotAvailableException("git repository");
                }
                repository = repositoryBuilder.build();

            } catch (IOException | CouldNotPerformException | NullPointerException ex) {

                if (!JPService.getProperty(JPInitializeDB.class).getValue()) {
                    throw ex;
                }
                repository = new FileRepositoryBuilder().create(databaseDirectory);
            }
            return repository;
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not detect git repo of Directory[" + databaseDirectory.getAbsolutePath() + "]!", ex);
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
            git.commit().setMessage(JPService.getApplicationName() + " commited all changes.").call();
        } catch (Exception ex) {
            throw new CouldNotPerformException("Could not commit all database changes!", ex);
        }
    }

    @Override
    public void checkAccess() throws InvalidStateException {
        try {
            if (isTag(getHead(repository))) {
                throw new InvalidStateException("Database based on tag revision and can not be modifiered!");
            }
        } catch (IOException ex) {
            throw new InvalidStateException("Could not access database!", ex);
        }
    }

    private static Ref getHead(final Repository repository) throws IOException {
        return repository.getRef(Constants.HEAD);
    }

    private static boolean isTag(final Ref ref) {
        return !ref.getTarget().getName().contains("refs/heads");
    }
}
