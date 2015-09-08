/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package de.citec.jul.storage.registry.plugin;

import de.citec.jps.core.JPService;
import de.citec.jul.exception.CouldNotPerformException;
import de.citec.jul.exception.printer.ExceptionPrinter;
import de.citec.jul.exception.InvalidStateException;
import de.citec.jul.exception.NotAvailableException;
import de.citec.jul.storage.file.FileSynchronizer;
import de.citec.jul.storage.registry.jp.JPInitializeDB;
import de.citec.jul.storage.registry.FileSynchronizedRegistry;
import de.citec.jul.storage.registry.jp.JPGitRegistryPluginRemoteURL;
import de.citec.jul.storage.registry.jp.JPResetDB;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author mpohling //
 */
public class GitRegistryPlugin extends FileRegistryPluginAdapter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private final FileSynchronizedRegistry registry;
    private final Git git;

    public GitRegistryPlugin(FileSynchronizedRegistry registry) throws de.citec.jul.exception.InstantiationException {
        try {
            this.registry = registry;
            this.git = detectGitRepository(registry.getDatabaseDirectory());
            this.initialSync();
        } catch (Exception ex) {
            shutdown();
            throw new de.citec.jul.exception.InstantiationException(this, ex);
        }
    }

    private void initialSync() throws CouldNotPerformException {
        try {
            this.git.pull().call().isSuccessful();
        } catch (GitAPIException ex) {
            throw new CouldNotPerformException("Initial sync failed!", ex);
        }
    }

    private Git detectGitRepository(final File databaseDirectory) throws CouldNotPerformException {
        try {
            
            Repository repo;
            try {
                // === load git out of db folder === //
                FileRepositoryBuilder repositoryBuilder = new FileRepositoryBuilder().findGitDir(databaseDirectory);

                if (repositoryBuilder.getGitDir() == null) {
                    throw new NotAvailableException("git repository");
                }
                repo = repositoryBuilder.build();

            } catch (IOException | CouldNotPerformException | NullPointerException ex) {
                logger.info("Could not find git repository in db Directory[" + databaseDirectory + "] for Registry[" + registry + "]!");

                // === load git out of remote url === //
                Map<String, String> remoteRepositoryMap = JPService.getProperty(JPGitRegistryPluginRemoteURL.class).getValue();

                if (remoteRepositoryMap.containsKey(registry.getName()) && !remoteRepositoryMap.get(registry.getName()).isEmpty()) {
                    logger.info("Cloning git repository from " + remoteRepositoryMap.get(registry.getName()) + " into db Directory[" + databaseDirectory + "] ...");
                    return Git.cloneRepository().setURI(remoteRepositoryMap.get(registry.getName())).setDirectory(databaseDirectory).call();
                }

                // ===  init new git === //
                if (!JPService.getProperty(JPInitializeDB.class).getValue()) {
                    throw ex;
                }
                repo = FileRepositoryBuilder.create(databaseDirectory);
            }
            return new Git(repo);
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
            if (isTag(getHead(git.getRepository()))) {
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

    @Override
    public final void shutdown() {
        if (git == null) {
            return;
        }
        try {
            git.getRepository().close();
            git.close();
        } catch (Exception ex) {
            ExceptionPrinter.printHistory(logger, new CouldNotPerformException("Could not shutdown", ex));
        }
    }
}
