/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.dc.jul.storage.registry.plugin;

import org.dc.jps.core.JPService;
import org.dc.jps.exception.JPServiceException;
import org.dc.jps.preset.JPTestMode;
import org.dc.jul.exception.CouldNotPerformException;
import org.dc.jul.exception.NotAvailableException;
import org.dc.jul.exception.RejectedException;
import org.dc.jul.exception.printer.ExceptionPrinter;
import org.dc.jul.exception.printer.LogLevel;
import org.dc.jul.iface.Identifiable;
import org.dc.jul.storage.file.FileSynchronizer;
import org.dc.jul.storage.registry.FileSynchronizedRegistry;
import org.dc.jul.storage.registry.RegistryInterface;
import org.dc.jul.storage.registry.jp.JPGitRegistryPluginRemoteURL;
import org.dc.jul.storage.registry.jp.JPInitializeDB;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.DetachedHeadException;
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
    private boolean detached;

    public GitRegistryPlugin(FileSynchronizedRegistry registry) throws org.dc.jul.exception.InstantiationException {
        try {
            this.detached = false;
            this.registry = registry;
            this.git = detectGitRepository(registry.getDatabaseDirectory());
            this.initialSync();
        } catch (Exception ex) {
            shutdown();
            throw new org.dc.jul.exception.InstantiationException(this, ex);
        }
    }

    @Override
    public void init(RegistryInterface reg) throws CouldNotPerformException {
    }

    private void initialSync() throws CouldNotPerformException {
        try {

            // sync main git
            try {
                this.git.pull().call().isSuccessful();
            } catch (DetachedHeadException ex) {
                detached = true;
            }

            // sync submodules
            try {
                this.git.submoduleInit();
                this.git.submoduleUpdate();
            } catch (Exception ex) {
                ExceptionPrinter.printHistory(new CouldNotPerformException("Could not init db submodules!", ex), logger, LogLevel.WARN);
            }
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
    public void afterRegister(Identifiable entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    @Override
    public void afterUpdate(Identifiable entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    @Override
    public void afterRemove(Identifiable entry, FileSynchronizer fileSynchronizer) throws CouldNotPerformException {
        commitAllChanges();
    }

    private void commitAllChanges() throws CouldNotPerformException {

        // Avoid commit in test mode.
        try {
            if (JPService.getProperty(JPTestMode.class).getValue()) {
                logger.warn("Skip commit because test mode is enabled!");
                return;
            }
        } catch (JPServiceException ex) {
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not access java property!", ex), logger);
        }

        // Avoid commit if branch is detached.
        if (detached) {
            logger.info("Skip commit because branch detached!");
            return;
        }

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
    public void checkAccess() throws RejectedException {
        try {
            if (isTag(getHead(git.getRepository()))) {
                throw new RejectedException("Database based on tag revision and can not be modifiered!");
            }

            if (detached) {
                throw new RejectedException("Database based on detached branch and can not be modifiered!");
            }
        } catch (IOException ex) {
            throw new RejectedException("Could not access database!", ex);
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
            ExceptionPrinter.printHistory(new CouldNotPerformException("Could not shutdown", ex), logger, LogLevel.ERROR);
        }
    }
}
